package com.qzx.xdupartner.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.BooleanUtil;
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
import com.qzx.xdupartner.entity.enumeration.HighTag;
import com.qzx.xdupartner.entity.vo.BlogVo;
import com.qzx.xdupartner.entity.vo.LowTagFrequencyVo;
import com.qzx.xdupartner.entity.vo.UserVo;
import com.qzx.xdupartner.exception.APIException;
import com.qzx.xdupartner.mapper.BlogMapper;
import com.qzx.xdupartner.service.BlogService;
import com.qzx.xdupartner.service.UserService;
import com.qzx.xdupartner.util.BlogUtil;
import com.qzx.xdupartner.util.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.qzx.xdupartner.constant.RedisConstant.BLOG_READ_KEY;
import static com.qzx.xdupartner.constant.RedisConstant.USER_BLOG_LIKED_KEY;
import static com.qzx.xdupartner.constant.SystemConstant.LIKE_PAGE_SIZE;
import static com.qzx.xdupartner.constant.SystemConstant.MAX_PAGE_SIZE;
import static com.qzx.xdupartner.util.BlogUtil.transBlogLowTag;

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
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    @Resource
    private BlogMapper blogMapper;
    @Resource
    private UserService userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private Boolean blogIsLiked(String id) {
        Long userId = UserHolder.getUserId();
//        log.warn(String.valueOf(userId));
        if (userId < 0) {
            return false;
        }
        return stringRedisTemplate.opsForSet().isMember(USER_BLOG_LIKED_KEY + userId, (id));
    }

    private Map<Object, Boolean> batchBlogIsLiked(List<String> blogIds) {
        Long userId = UserHolder.getUserId();
//        log.warn(String.valueOf(userId));
        if (userId < 0) {
            return new HashMap<>();
        }
        if (blogIds.isEmpty())
            return new HashMap<>();
        Map<Object, Boolean> isLiked = stringRedisTemplate.opsForSet().isMember(USER_BLOG_LIKED_KEY + userId,
                blogIds.toArray());
        return isLiked;
    }


    private List<BlogVo> getBlogVos(List<Blog> records, String redisKey) {
        if (records == null || records.isEmpty()) {
            return ListUtil.empty();
        }
        Set<String> viewed = stringRedisTemplate.opsForSet().members(redisKey + UserHolder.getUserId());
        if (viewed != null && !viewed.isEmpty()) {
            records =
                    records.stream().filter(blog -> !viewed.contains(String.valueOf(blog.getId()))).collect(Collectors.toList());
        }
        List<String> blogIds =
                records.stream().map(record -> String.valueOf(record.getId())).collect(Collectors.toList());
        executor.submit(() -> {
            batchAddViewTimes(blogIds, UserHolder.getUserId());
        });
        List<String> userIds =
                records.stream().map(record -> String.valueOf(record.getUserId())).distinct().collect(Collectors.toList());
        if (UserHolder.getUserId() > 0 && !blogIds.isEmpty()) {
            stringRedisTemplate.opsForSet().add(redisKey + UserHolder.getUserId(), blogIds.toArray(new String[0]));
            stringRedisTemplate.expire(redisKey + UserHolder.getUserId(), RedisConstant.USER_BLOG_SET_TIME,
                    TimeUnit.SECONDS);
        }
        Map<Long, UserVo> userVoMap =
                userIds.stream().map(userId -> userService.getUserVoById(Long.valueOf(userId))).collect(Collectors.toMap(UserVo::getId, userVo -> userVo));
        Map<Object, Boolean> isLikedMap = batchBlogIsLiked(blogIds);
        return records.stream().map(blog -> {
            BlogVo blogVo = BeanUtil.copyProperties(blog, BlogVo.class);
            if (blog.getIsAnonymous() == 0) {
                blogVo.setUserVo(userVoMap.get(blog.getUserId()));
            } else {
                UserVo anonymousVo = UserVo.getAnonymousVo();
                blogVo.setUserVo(anonymousVo);
            }
            blogVo.setImages(BlogUtil.transBlogImg(blog))
                    .setLowTags(transBlogLowTag(blog))
                    .setHighTag(blog.getHighTagId())
                    .setHighTagValue(HighTag.match(blog.getHighTagId()).getDisplay())
                    .setIsLiked(BooleanUtil.isTrue(isLikedMap.get(String.valueOf(blog.getId())))).setViewTimes(blogVo.getViewTimes());
            return blogVo;
        }).collect(Collectors.toList());
    }

    private void AddViewTimes(Long blogId, Long userId) {
        //5分钟为窗口期
        //使用HLL, 增加定时任务
        //key格式 blog:read:{时间除(5*60*1000)值}:{blogId值} key存活时间10min 在这1分钟内进行定时任务
        //定时任务: 将 上述格式:{blogId}的浏览量落表
        DateTime nowDate = DateUtil.date();
        long betweenDayStart = nowDate.between(DateUtil.beginOfDay(nowDate), DateUnit.SECOND);
        String pattern = ((betweenDayStart / (5 * 60)) + 1) + ":";
        stringRedisTemplate.opsForSet().add(BLOG_READ_KEY + pattern + blogId, String.valueOf(userId));
    }

    private void batchAddViewTimes(List<String> blogIds, Long userId) {
        DateTime nowDate = DateUtil.date();
        long betweenDayStart = nowDate.between(DateUtil.beginOfDay(nowDate), DateUnit.SECOND);
        String pattern = ((betweenDayStart / (5 * 60)) + 1) + ":";
        String redisKey = BLOG_READ_KEY + pattern;
        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            StringRedisConnection stringRedisConnection = (StringRedisConnection) connection;
            blogIds.forEach(blogId -> {
                stringRedisConnection.sAdd(redisKey + blogId, String.valueOf(userId));
                stringRedisConnection.expire(redisKey + blogId, 6 * 60);
            });
            return null;
        });
    }

    private BlogVo transferToBlogVo(Blog blog) {
        BlogVo blogVo = BeanUtil.copyProperties(blog, BlogVo.class);
        if (blog.getIsAnonymous() == 0) {
            blogVo.setUserVo(userService.getUserVoById(blog.getUserId()));
        } else {
            UserVo anonymousVo = UserVo.getAnonymousVo();
            blogVo.setUserVo(anonymousVo);
        }
        //image
        String imageStr = blog.getImages();
        if (StrUtil.isNotBlank(imageStr)) {
            List<String> imageUris =
                    Arrays.stream(imageStr.split(SystemConstant.PICTURE_CONJUNCTION)).collect(Collectors.toList());
            blogVo.setImages(imageUris);
        }
        //lowTag
        List<String> lowTagList = transBlogLowTag(blog);
        blogVo.setLowTags(lowTagList);
        blogVo.setHighTag(blog.getHighTagId());
//        boolean isLiked = blogIsLiked(blog.getId());
//        blogVo.setIsLiked(isLiked);
        return blogVo;
    }


    @Override
    public void saveBlog(Blog blog, List<String> tagWords) {
        save(blog);
        String redisKey = RedisConstant.LOW_TAG_FREQUENCY + blog.getHighTagId();
//        executor.submit(() -> {
        stringRedisTemplate.executePipelined(new RedisCallback<Object>() {
            @Nullable
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                StringRedisConnection stringRedisConnection = (StringRedisConnection) redisConnection;
                tagWords.forEach(word -> {
                    stringRedisConnection.hIncrBy(redisKey, word, 1);
                });
                return null;
            }
        });
//        });
    }

    @Override
    public boolean completeBlog(Long id) {
        return update().eq("user_id", UserHolder.getUserId()).eq("id", id).set("is_complete", 1).update();
    }

    @Override
    public boolean likeBlog(Long id) {
        String likedKey = USER_BLOG_LIKED_KEY + UserHolder.getUserId();
        boolean isLiked = blogIsLiked(String.valueOf(id));
        if (!isLiked) {//点赞
            update().eq("id", id).setSql("liked = liked + 1").update();
            stringRedisTemplate.opsForSet().add(likedKey, String.valueOf(id));
            return true;
        } else {//取消点赞
            update().eq("id", id).setSql("liked = liked - 1").update();
            stringRedisTemplate.opsForSet().remove(likedKey, String.valueOf(id));
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
            Blog byId = getById(id);
            if (ObjectUtil.isNull(byId)) {
                throw new APIException("该帖子不存在");
            }
            BlogVo blogVo = BlogUtil.convertToBlogVo(byId);
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
