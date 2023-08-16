package com.qzx.xdupartner.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.qzx.xdupartner.constant.SystemConstant;
import com.qzx.xdupartner.entity.Blog;
import com.qzx.xdupartner.entity.dto.BlogDto;
import com.qzx.xdupartner.entity.vo.BlogVo;
import com.qzx.xdupartner.entity.vo.LowTagFrequencyVo;
import com.qzx.xdupartner.exception.ApiException;
import com.qzx.xdupartner.exception.ParamErrorException;
import com.qzx.xdupartner.service.BlogService;
import com.qzx.xdupartner.util.UserHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author qzx
 * @since 2023-08-12
 */
@RestController
@RequestMapping("/blog")
public class BlogController {
    private static final JiebaSegmenter segmenter = new JiebaSegmenter();

    @Resource
    private BlogService blogService;

    private void checkBlogDtoParam(BlogDto blogDto) {
        List<String> lowTags = blogDto.getLowTags();
        if (lowTags != null && lowTags.size() > 9) {
            throw new ParamErrorException("二级标签最多为9个");
        }
        List<String> imageList = blogDto.getImageList();
        if (imageList != null && imageList.size() > 9) {
            throw new ParamErrorException("图片最多为9张");
        }
    }

    private List<String> doExplainTags(BlogDto blogDto) {
        //TODO 将blogTag用分词器进行分析
        List<String> lowTags = blogDto.getLowTags();
        List<String> collects = new ArrayList<>(10);
        if (lowTags != null) {
            lowTags.forEach(tag -> {
//                List<SegToken> process = segmenter.process(tag, JiebaSegmenter.SegMode.INDEX);
//                collects.addAll(process.stream().map(segToken -> segToken.word).collect(Collectors.toList()));
                List<String> process = segmenter.sentenceProcess(tag);
                collects.addAll(process);
            });
        }
        return collects;
    }

    private Blog transferToBlogClass(BlogDto blogDto) {
        Blog blog = BeanUtil.copyProperties(blogDto, Blog.class);
        blog.setUserId(UserHolder.getUserId());
        List<String> lowTags = blogDto.getLowTags();
        if (lowTags != null) {
            String lowTagStr = StrUtil.join(SystemConstant.LOW_TAG_CONJUNCTION, lowTags);
            blog.setLowTags(lowTagStr);
        }
        List<String> imageList = blogDto.getImageList();
        if (imageList != null) {
//            List<String> imageIds = imageList.stream().map(AesUtil::decryptHex).collect(Collectors.toList());
//            String images = StrUtil.join(SystemConstant.PICTURE_CONJUNCTION, imageIds);
            String images = StrUtil.join(SystemConstant.PICTURE_CONJUNCTION, imageList);
            blog.setImages(images);
        }
        return blog;
    }

    @GetMapping("/like/{id}")
    public String likeBlog(@Validated @PathVariable("id") Long id) {
        boolean isLike = blogService.likeBlog(id);
        return isLike ? "点赞成功" : "取消点赞成功";
    }

    @PostMapping(value = "/pubBlog", produces = "application/json;charset=utf-8")
    public String publishBlog(@Validated @RequestBody @NotNull(message = "需要提交帖子") BlogDto blogDto) {
        checkBlogDtoParam(blogDto);
        Blog blog = transferToBlogClass(blogDto);
        blogService.saveBlog(blog, doExplainTags(blogDto));
        return "发布成功";
    }


    @GetMapping(value = "/complete", produces = "application/json;charset=utf-8")
    public String completeBlog(@Validated @RequestParam @NotNull Long id) {
        boolean isUpdate = blogService.completeBlog(id);
        return isUpdate ? "恭喜您完成了！" : "更新帖子状态失败";

    }

    @PostMapping(value = "/update/{id}", produces = "application/json;charset=utf-8")
    public String updateBlog(@PathVariable Long id,
                             @Validated @RequestBody @NotNull(message = "需要提交帖子") BlogDto blogDto) {
        checkBlogDtoParam(blogDto);
        Blog blog = transferToBlogClass(blogDto);
        blog.setId(id);
        boolean isUpdate = blogService.updateBlog(blog);
        if (!isUpdate) {
            throw new ApiException("更新失败");
        }
        return "更新成功";
    }

    @GetMapping(value = "/delete", produces = "application/json;charset=utf-8")
    public String deleteBlog(Long id) {
        boolean isDelete = blogService.deleteBlog(id);
        if (!isDelete) {
            throw new ApiException("删除失败");
        }
        return "删除成功";
    }

    @GetMapping("/query/{id}")
    public BlogVo queryBlog(@Validated @PathVariable("id") @NotBlank(message = "帖子id不能为空") Long id) {
        return blogService.getVoById(id);
    }

    @GetMapping(value = "/queryOnesBlog", produces = "application/json;charset=utf-8")
    public List<BlogVo> queryOnesBlogs(@RequestParam(value = "current", defaultValue = "1") Integer current,
                                       @RequestParam Long userId) {
        return blogService.getOnesBlogVo(userId, current);
    }

    @GetMapping(value = "/queryHottestBlog", produces = "application/json;charset=utf-8")
    public List<BlogVo> queryHottestBlogs(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return blogService.getHottest(current);
    }

    @GetMapping(value = "/queryNewestBlog", produces = "application/json;charset=utf-8")
    public List<BlogVo> queryNewestBlogs(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return blogService.getNewest(current);
    }

    @GetMapping(value = "/queryLikeBlog", produces = "application/json;charset=utf-8")
    public List<BlogVo> queryLikeBlogs(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return blogService.getLike(current);
    }

    @PostMapping(value = "/searchBlog", produces = "application/json;charset=utf-8")
    public List<BlogVo> searchBlog(@Validated @NotBlank(message = "搜索词不能为空") @RequestParam String keyword,
                                   @RequestParam(value = "current", defaultValue = "1") Integer current) {
        return blogService.search(keyword, current);
    }

    @GetMapping(value = "/searchTagWordByTypeId", produces = "application/json;charset=utf-8")
    public List<BlogVo> searchLowTagsByTypeId(@Validated @DecimalMax(value = "4", message = "一级标签类型不合法") @DecimalMin(value =
            "1", message = "一级标签类型不合法") @RequestParam Integer typeId,
                                              @Validated @NotNull(message = "搜索词不能为空") String keyword,
                                              @RequestParam(value =
                                                      "current", defaultValue = "1") Integer current) {
        if (keyword.equals("all"))
            keyword = "";
        return blogService.searchByTypeIdContainsLowTag(typeId, keyword, current);
    }

    @GetMapping(value = "/getTagWordCount", produces = "application/json;charset=utf-8")
    public List<LowTagFrequencyVo> getTagWordCount(
            @Validated @DecimalMax(value = "4", message = "一级标签类型不合法") @DecimalMin(value = "1", message = "一级标签类型不合法")
            @RequestParam Integer typeId) {
        return blogService.getTagWordCount(typeId);
    }
}

