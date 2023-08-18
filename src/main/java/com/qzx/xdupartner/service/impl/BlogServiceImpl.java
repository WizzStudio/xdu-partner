package com.qzx.xdupartner.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzx.xdupartner.constant.RedisConstant;
import com.qzx.xdupartner.constant.SystemConstant;
import com.qzx.xdupartner.entity.Blog;
import com.qzx.xdupartner.entity.User;
import com.qzx.xdupartner.entity.vo.BlogVo;
import com.qzx.xdupartner.entity.vo.LowTagFrequencyVo;
import com.qzx.xdupartner.entity.vo.UserVo;
import com.qzx.xdupartner.exception.ApiException;
import com.qzx.xdupartner.mapper.BlogMapper;
import com.qzx.xdupartner.service.BlogService;
import com.qzx.xdupartner.service.UserService;
import com.qzx.xdupartner.util.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.qzx.xdupartner.constant.RedisConstant.USESR_BLOG_LIKED_KEY;
import static com.qzx.xdupartner.constant.SystemConstant.LIKE_PAGE_SIZE;
import static com.qzx.xdupartner.constant.SystemConstant.MAX_PAGE_SIZE;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author qzx
 * @since 2023-08-12
 */
@Service
@Slf4j
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
    @Resource
    private BlogMapper blogMapper;
    @Resource
    private UserService userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private boolean blogIsLiked(Long id) {
        Long userId = UserHolder.getUserId();
//        log.warn(String.valueOf(userId));
        if (userId < 0) {
            return false;
        }
        Long rank = stringRedisTemplate.opsForZSet().rank(USESR_BLOG_LIKED_KEY + userId, String.valueOf(id));
//        log.warn(id+" isLiked:"+rank);
        return rank != null;
    }

    private List<BlogVo> getBlogVos(List<Blog> records, String redisKey) {
        if (records == null || records.size() == 0) return new ArrayList<>();
        Set<String> viewed = stringRedisTemplate.opsForSet().members(redisKey + UserHolder.getUserId());
        String[] idStrings = records.stream().map(blog -> String.valueOf(blog.getId())).toArray(String[]::new);
        stringRedisTemplate.opsForSet().add(redisKey + UserHolder.getUserId(), idStrings);
        stringRedisTemplate.expire(redisKey + UserHolder.getUserId(), RedisConstant.USER_BLOG_SET_TIME,
                TimeUnit.SECONDS);
        List<BlogVo> voRecords = new ArrayList<>(MAX_PAGE_SIZE);
        records.forEach(blog -> {
            //当显示过的时候跳过
            if (viewed == null || !viewed.contains(String.valueOf(blog.getId()))) {
                //TODO 分析lowTags
                BlogVo blogVo = transferToBlogVo(blog);
                String readKey = RedisConstant.BLOG_READ_KEY + blogVo.getId();
                blogVo.setViewTimes((int) (blogVo.getViewTimes() + stringRedisTemplate.opsForValue().increment(readKey)));
                voRecords.add(blogVo);
                ;
            }
        });
        return voRecords;
    }

    private BlogVo transferToBlogVo(Blog blog) {
        BlogVo blogVo = BeanUtil.copyProperties(blog, BlogVo.class);
        if (blog.getIsAnonymous() == 0) {
            blogVo.setUserVo(userService.getUserVoById(blog.getUserId()));
        } else {
            UserVo anonymousVo = UserVo.getAnonymousVo();
            blogVo.setUserVo(anonymousVo);;
        }
        //image
        String imageStr = blog.getImages();
        if (StrUtil.isNotBlank(imageStr)) {
            List<String> imageUris =
                    Arrays.stream(imageStr.split(SystemConstant.PICTURE_CONJUNCTION)).collect(Collectors.toList());
            blogVo.setImages(imageUris);
        }
        //lowTag
        List<String> lowTagList =
                Arrays.stream(blog.getLowTags().split(SystemConstant.LOW_TAG_CONJUNCTION)).collect(Collectors.toList());
        blogVo.setLowTags(lowTagList);
        blogVo.setHighTag(blog.getHighTagId());
        boolean isLiked = blogIsLiked(blog.getId());
        blogVo.setIsLiked(isLiked);
        return blogVo;
    }


    @Override
    public void saveBlog(Blog blog, List<String> tagWords) {
        save(blog);
        String redisKey = RedisConstant.LOW_TAG_FREQUENCY + blog.getHighTagId();
        executor.submit(new Thread(() -> {
            tagWords.forEach(word -> {
                stringRedisTemplate.opsForHash().increment(redisKey, word, 1);
            });
        }));
    }

    @Override
    public boolean completeBlog(Long id) {
        return update().eq("user_id", UserHolder.getUserId()).eq("id", id).set("is_complete", 1).update();
    }

    @Override
    public boolean likeBlog(Long id) {
        String likedKey = USESR_BLOG_LIKED_KEY + UserHolder.getUserId();
        boolean isLiked = blogIsLiked(id);
        if (!isLiked) {//点赞
            boolean suc = update().eq("id", id).setSql("liked = liked + 1").update();
            if (suc) {
                stringRedisTemplate.opsForZSet().add(likedKey, String.valueOf(id), System.currentTimeMillis());
            }
            return true;
        } else {//取消点赞
            boolean suc = update().eq("id", id).setSql("liked = liked - 1").update();
            if (suc) {
                stringRedisTemplate.opsForZSet().remove(likedKey, String.valueOf(id));
            }
            return false;
        }
    }

    @Override
    public boolean updateBlog(Blog blog) {
        //TODO 分词器分析
        boolean update =
                update().eq("id", blog.getId()).eq("user_id", UserHolder.getUserId()).update(blog);
        if (update) {
            stringRedisTemplate.delete(RedisConstant.BLOG_CACHE + blog.getId());
        }
        return update;
    }

    @Override
    public boolean deleteBlog(Long id) {
        //TODO 回滚分词器
        HashMap<String, Object> columnMap = new HashMap<>();
        columnMap.put("id", id);
        columnMap.put("user_id", UserHolder.getUserId());
        boolean suc = removeByMap(columnMap);
        if (suc) {
            stringRedisTemplate.delete(RedisConstant.BLOG_CACHE + id);
        }
        return suc;
    }

    @Override
    public BlogVo getVoById(Long id) {
        String key = RedisConstant.BLOG_CACHE + id;
        String jsonBlogVo = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(jsonBlogVo)) {
            return JSONUtil.toBean(jsonBlogVo, BlogVo.class);
        } else {
            Blog byId = super.getById(id);
            if (ObjectUtil.isNull(byId)) {
                throw new ApiException("该帖子不存在");
            }
            BlogVo blogVo = transferToBlogVo(byId);
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(blogVo),
                    RandomUtil.randomInt(SystemConstant.RANDOM_TTL_MIN, SystemConstant.RANDOM_TTL_MAX),
                    TimeUnit.SECONDS);
            return blogVo;
        }
    }

    @Override
    public List<LowTagFrequencyVo> getTagWordCount(Integer typeId) {
        Map<Object, Object> wordFrequencyMap =
                stringRedisTemplate.opsForHash().entries(RedisConstant.LOW_TAG_FREQUENCY + typeId);
        return wordFrequencyMap.entrySet().stream().map(entry ->
                        new LowTagFrequencyVo(
                                (String) entry.getKey(),
                                Long.valueOf((String) entry.getValue())))
                .sorted((a, b) -> Math.toIntExact(b.getCount() - a.getCount()))
                .limit(10).collect(Collectors.toList());
    }

    @Override
    public List<BlogVo> search(String keyword, Integer current) {
        if (current == 1) {
            stringRedisTemplate.delete(RedisConstant.USER_SEARCH_BLOG_SET_KEY + UserHolder.getUserId());
        }
        QueryWrapper<Blog> blogQueryWrapper = new QueryWrapper<>();
        blogQueryWrapper
                .like("title", keyword)
                .or()
                .like("content", keyword)
                .or()
                .like("low_tags", keyword)
                .orderByDesc("id");
        Page<Blog> page = blogMapper.selectPage(new Page<>(current,
                MAX_PAGE_SIZE), blogQueryWrapper);
        List<Blog> records = page.getRecords();
        return getBlogVos(records, RedisConstant.USER_SEARCH_BLOG_SET_KEY);
    }

    @Override
    public List<BlogVo> searchByTypeIdContainsLowTag(Integer typeId, String keyword, Integer current) {
        if (current == 1) {
            stringRedisTemplate.delete(RedisConstant.USER_SEARCH_BLOG_SET_KEY + UserHolder.getUserId());
        }
        QueryWrapper<Blog> blogQueryWrapper = new QueryWrapper<>();
        blogQueryWrapper
                .like("low_tags", keyword)
                .eq("high_tag_id", typeId)
                .orderByDesc("id");
        Page<Blog> page = blogMapper.selectPage(new Page<>(current,
                MAX_PAGE_SIZE), blogQueryWrapper);
        List<Blog> records = page.getRecords();
        return getBlogVos(records, RedisConstant.USER_SEARCH_BLOG_SET_KEY);
    }

    @Override
    public List<BlogVo> getOnesBlogVo(Long userId, Integer current) {
        if (current == 1) {
            stringRedisTemplate.delete(RedisConstant.USER_ONES_BLOG_SET_KEY + UserHolder.getUserId());
        }
        Page<Blog> page;
        if (!userId.equals(UserHolder.getUserId())) {
            page = query().eq("user_id", userId)
                    .eq("is_anonymous", 0)
                    .orderByDesc("id").page(new Page<>(current,
                            MAX_PAGE_SIZE));
        } else {
            page = query().eq("user_id", userId)
                    .orderByDesc("id").page(new Page<>(current,
                            MAX_PAGE_SIZE));
        }
        List<Blog> records = page.getRecords();
        return getBlogVos(records, RedisConstant.USER_ONES_BLOG_SET_KEY);
    }


    @Override
    public List<BlogVo> getHottest(Integer current) {
        if (current == 1) {
            stringRedisTemplate.delete(RedisConstant.USER_HOT_BLOG_SET_KEY + UserHolder.getUserId());
        }
        Page<Blog> page = query()
                .orderBy(true, false, "(liked * 3 + view_times * 1) / LN( TIMESTAMPDIFF (DAY, create_time, " +
                        "CURRENT_TIMESTAMP) +3)")
                .orderByDesc("id")
                .page(new Page<>(current, MAX_PAGE_SIZE));
        List<Blog> records = page.getRecords();
        return getBlogVos(records, RedisConstant.USER_HOT_BLOG_SET_KEY);
    }

    @Override
    public List<BlogVo> getNewest(Integer current) {
        if (current == 1) {
            stringRedisTemplate.delete(RedisConstant.USER_NEW_BLOG_SET_KEY + UserHolder.getUserId());
        }
        Page<Blog> page = query()
                .orderByDesc("id")
                .page(new Page<>(current, MAX_PAGE_SIZE));
        List<Blog> records = page.getRecords();
        return getBlogVos(records, RedisConstant.USER_NEW_BLOG_SET_KEY);
    }

    @Override
    public List<BlogVo> getLike(Integer current) {
        if (current == 1) {
            stringRedisTemplate.delete(RedisConstant.USER_LIKE_BLOG_SET_KEY + UserHolder.getUserId());
        }
        User user = UserHolder.getUser();
        Integer highTag = null;
        if (user != null) {
            highTag = user.getHighTag();
        }
        if (highTag == null || highTag == 0) {
            highTag = 1;
        }
        Page<Blog> page = query().eq("high_tag_id", highTag).orderByDesc("id").page(new Page<>(current,
                LIKE_PAGE_SIZE));
        List<Blog> records = page.getRecords();
        Page<Blog> page1 = query().ne("high_tag_id", highTag).orderByDesc("id").page(new Page<>(current,
                MAX_PAGE_SIZE - Math.min(LIKE_PAGE_SIZE, records == null ? 0 : records.size())));
        List<Blog> records1 = page1.getRecords();
        List<Blog> res = new ArrayList<>(10);
        if (records != null) {
            res.addAll(records);
        }
        if (records1 != null) {
            res.addAll(records1);
        }
        res.sort(Comparator.comparingLong(Blog::getId).reversed());
        return getBlogVos(res, RedisConstant.USER_LIKE_BLOG_SET_KEY);
    }
}
