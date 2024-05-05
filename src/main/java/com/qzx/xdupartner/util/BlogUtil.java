package com.qzx.xdupartner.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.qzx.xdupartner.constant.SystemConstant;
import com.qzx.xdupartner.entity.Blog;
import com.qzx.xdupartner.entity.dto.BlogDto;
import com.qzx.xdupartner.entity.vo.BlogVo;
import com.qzx.xdupartner.entity.vo.UserVo;
import com.qzx.xdupartner.exception.ParamErrorException;
import com.qzx.xdupartner.service.UserService;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.qzx.xdupartner.constant.RedisConstant.USER_BLOG_LIKED_KEY;

public class BlogUtil {
    private static final JiebaSegmenter segment = new JiebaSegmenter();

    public static void checkListInBlogDto(BlogDto blogDto) {
        List<String> lowTags = blogDto.getLowTags();
        if (ObjectUtil.isNotEmpty(lowTags) && lowTags.size() > 9) {
            throw new ParamErrorException("二级标签最多为9个");
        }
        List<String> imageList = blogDto.getImageList();
        if (ObjectUtil.isNotEmpty(imageList) && imageList.size() > 9) {
            throw new ParamErrorException("图片最多为9张");
        }
    }

    public static Blog convertToBlog(BlogDto blogDto) {
        return BeanUtil.copyProperties(blogDto, Blog.class)
                .setUserId(UserHolder.getUserId())
                .setLowTags(StrUtil.join(SystemConstant.LOW_TAG_CONJUNCTION, blogDto.getLowTags()))
                .setImages(StrUtil.join(SystemConstant.PICTURE_CONJUNCTION, blogDto.getImageList()));
    }

    public static List<String> doExplainTags(BlogDto blogDto) {
        List<String> lowTags = blogDto.getLowTags();
        List<String> collects = new ArrayList<>(10);
        if (lowTags != null) {
            lowTags.forEach(tag -> {
                List<String> process = segment.sentenceProcess(tag);
                collects.addAll(process);
            });
        }
        return collects;
    }

    public static BlogVo convertToBlogVo(Blog blog) {
        BlogVo blogVo = BeanUtil.copyProperties(blog, BlogVo.class)
                .setImages(transBlogImg(blog))
                .setLowTags(transBlogLowTag(blog))
                .setHighTag(blog.getHighTagId())
                .setIsLiked(blogIsLiked(blog.getId()));
        UserVo userVo = UserVo.getAnonymousVo();
        if (blog.getIsAnonymous() == 0) {
            UserService userService = SpringUtil.getBean(UserService.class);
            userVo = userService.getUserVoById(blog.getUserId());
        }
        blogVo.setUserVo(userVo);
        return blogVo;
    }

    private static boolean blogIsLiked(Long id) {
        Long userId = UserHolder.getUserId();
        if (userId < 0) {
            return false;
        }
        StringRedisTemplate stringRedisTemplate = SpringUtil.getBean(StringRedisTemplate.class);
        return Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(USER_BLOG_LIKED_KEY + userId, (id)));
    }

    public static List<String> transBlogImg(Blog blog) {
        //image
        String imageStr = blog.getImages();
        return StrUtil.isBlank(imageStr) ? ListUtil.empty() :
                Arrays.stream(imageStr.split(SystemConstant.PICTURE_CONJUNCTION)).collect(Collectors.toList());
    }

    public static List<String> transBlogLowTag(Blog blog) {
        String lowTags = blog.getLowTags();
        return StrUtil.isBlank(lowTags) ? ListUtil.empty() :
                Arrays.stream(lowTags.split(SystemConstant.LOW_TAG_CONJUNCTION)).collect(Collectors.toList());
    }
}
