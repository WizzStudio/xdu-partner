package com.qzx.xdupartner.controller;


import com.qzx.xdupartner.entity.Blog;
import com.qzx.xdupartner.entity.dto.BlogDto;
import com.qzx.xdupartner.entity.vo.BlogVo;
import com.qzx.xdupartner.entity.vo.LowTagFrequencyVo;
import com.qzx.xdupartner.entity.vo.R;
import com.qzx.xdupartner.entity.vo.ResultCode;
import com.qzx.xdupartner.exception.APIException;
import com.qzx.xdupartner.service.BlogService;
import com.qzx.xdupartner.service.UserService;
import com.qzx.xdupartner.util.RUtil;
import com.qzx.xdupartner.util.UserHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

import static com.qzx.xdupartner.util.BlogUtil.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author qzx
 * @since 2023-08-12
 */
@Api("帖子控制层")
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Resource
    private BlogService blogService;
    @Resource
    private UserService userService;

    @ApiOperation("点赞")
    @GetMapping("/like/{id}")
    public R<String> likeBlog(@Validated @PathVariable("id") Long id) {
        boolean isLike = blogService.likeBlog(id);
        return RUtil.success(isLike ? "点赞成功" : "取消点赞成功");
    }


    @ApiOperation("发布帖子")
    @PostMapping(value = "/publish", produces = "application/json;charset=utf-8")
    public R<String> publishBlogV2(@Validated @RequestBody @NotNull(message = "需要提交帖子") BlogDto blogDto) {
        if (!userService.checkUserIsVerified(UserHolder.getUserId())) {
            return RUtil.error(ResultCode.UNVERIFIED_ERROR);
        }
        checkListInBlogDto(blogDto);
        Blog blog = convertToBlog(blogDto);
        blogService.saveBlog(blog, doExplainTags(blogDto));
        return RUtil.success("发布成功");
    }


    @ApiOperation("完成帖子")
    @GetMapping(value = "/complete", produces = "application/json;charset=utf-8")
    public R<String> completeBlog(@Validated @RequestParam @NotNull Long id) {
        boolean isUpdate = blogService.completeBlog(id);
        return RUtil.success(isUpdate ? "恭喜您完成了！" : "更新帖子状态失败");
    }

    @ApiOperation("更新帖子")
    @PostMapping(value = "/update/{id}", produces = "application/json;charset=utf-8")
    public R<String> updateBlog(@PathVariable Long id,
                                @Validated @RequestBody @NotNull(message = "需要提交帖子") BlogDto blogDto) {
        checkListInBlogDto(blogDto);
        Blog blog = convertToBlog(blogDto).setId(id);
        boolean isUpdate = blogService.updateBlog(blog);
        if (!isUpdate) {
            throw new APIException(ResultCode.FAILED);
        }
        return RUtil.success("更新成功");
    }

    @ApiOperation("删除帖子")
    @GetMapping(value = "/delete", produces = "application/json;charset=utf-8")
    public R<String> deleteBlog(Long id) {
        boolean isDelete = blogService.deleteBlog(id);
        if (!isDelete) {
            throw new APIException("删除失败");
        }
        return RUtil.success("删除成功");
    }

    @ApiOperation("按id查询帖子")
    @GetMapping("/query/id/{id}")
    public R<BlogVo> queryBlog(@Validated @PathVariable("id") @NotBlank(message = "帖子id不能为空") Long id) {
        return RUtil.success(blogService.getVoById(id));
    }


    @ApiOperation("查询某个用户的帖子")
    @GetMapping(value = "/query/one", produces = "application/json;charset=utf-8")
    public R<List<BlogVo>> queryOnesBlogsV2(@RequestParam(value = "current", defaultValue = "1") Integer current,
                                            @RequestParam Long userId) {
        return RUtil.success(blogService.getOnesBlogVo(userId, current));
    }


    @ApiOperation("查询最热帖子")
    @GetMapping(value = "/query/hot", produces = "application/json;charset=utf-8")
    public R<List<BlogVo>> queryHottestBlogsV2(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return RUtil.success(blogService.getHottest(current));
    }


    @ApiOperation("查询最新帖子")
    @GetMapping(value = "/query/new", produces = "application/json;charset=utf-8")
    public R<List<BlogVo>> queryNewestBlogs(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return RUtil.success(blogService.getNewest(current));
    }


    @ApiOperation("查询感兴趣的帖子")
    @GetMapping(value = "/query/like", produces = "application/json;charset=utf-8")
    public R<List<BlogVo>> queryLikeBlogsV2(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return RUtil.success(blogService.getLike(current));
    }


    @ApiOperation("搜索帖子")
    @PostMapping(value = "/search", produces = "application/json;charset=utf-8")
    public R<List<BlogVo>> searchBlogV2(@Validated @NotBlank(message = "搜索词不能为空") @RequestParam String keyword,
                                        @RequestParam(value = "current", defaultValue = "1") Integer current) {
        return RUtil.success(blogService.search(keyword, current));
    }


    @ApiOperation("分类下搜索帖子")
    @GetMapping(value = "/tag/search/type", produces = "application/json;charset=utf-8")
    public R<List<BlogVo>> searchLowTagsByTypeIdV2(@Validated @DecimalMax(value = "4", message = "一级标签类型不合法") @DecimalMin(value =
            "1", message = "一级标签类型不合法") @RequestParam Integer typeId,
                                                   @Validated @NotNull(message = "搜索词不能为空") String keyword,
                                                   @RequestParam(value =
                                                           "current", defaultValue = "1") Integer current) {
        if (keyword.equals("all")) {
            keyword = "";
        }
        return RUtil.success(blogService.searchByTypeIdContainsLowTag(typeId, keyword, current));
    }

    @ApiOperation("某个分类下分词词频")
    @GetMapping(value = "/tag/count", produces = "application/json;charset=utf-8")
    public R<List<LowTagFrequencyVo>> getTagWordCountV2(
            @Validated @DecimalMax(value = "4", message = "一级标签类型不合法") @DecimalMin(value = "1", message = "一级标签类型不合法")
            @RequestParam Integer typeId) {
        return RUtil.success(blogService.getTagWordCount(typeId));
    }

    @Deprecated
    @ApiOperation("查询最热帖子")
    @GetMapping(value = "/queryHottestBlog", produces = "application/json;charset=utf-8")
    public R<List<BlogVo>> queryHottestBlogs(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return RUtil.success(blogService.getHottest(current));
    }

    @Deprecated
    @ApiOperation("查询最新帖子")
    @GetMapping(value = "/queryNewestBlog", produces = "application/json;charset=utf-8")
    public R<List<BlogVo>> queryNewestBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return RUtil.success(blogService.getNewest(current));
    }

    @Deprecated
    @ApiOperation("查询感兴趣的帖子")
    @GetMapping(value = "/queryLikeBlog", produces = "application/json;charset=utf-8")
    public R<List<BlogVo>> queryLikeBlogs(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return RUtil.success(blogService.getLike(current));
    }

    @Deprecated
    @ApiOperation("搜索帖子")
    @PostMapping(value = "/searchBlog", produces = "application/json;charset=utf-8")
    public R<List<BlogVo>> searchBlog(@Validated @NotBlank(message = "搜索词不能为空") @RequestParam String keyword,
                                      @RequestParam(value = "current", defaultValue = "1") Integer current) {
        return RUtil.success(blogService.search(keyword, current));
    }

    @Deprecated
    @ApiOperation("发布帖子")
    @PostMapping(value = "/pubBlog", produces = "application/json;charset=utf-8")
    public R<String> publishBlog(@Validated @RequestBody @NotNull(message = "需要提交帖子") BlogDto blogDto) {
        checkListInBlogDto(blogDto);
        Blog blog = convertToBlog(blogDto);
        blogService.saveBlog(blog, doExplainTags(blogDto));
        return RUtil.success("发布成功");
    }

    @Deprecated
    @ApiOperation("分类下搜索帖子")
    @GetMapping(value = "/searchTagWordByTypeId", produces = "application/json;charset=utf-8")
    public R<List<BlogVo>> searchLowTagsByTypeId(@Validated @DecimalMax(value = "4", message = "一级标签类型不合法") @DecimalMin(value =
            "1", message = "一级标签类型不合法") @RequestParam Integer typeId,
                                                 @Validated @NotNull(message = "搜索词不能为空") String keyword,
                                                 @RequestParam(value =
                                                         "current", defaultValue = "1") Integer current) {
        if (keyword.equals("all")) {
            keyword = "";
        }
        return RUtil.success(blogService.searchByTypeIdContainsLowTag(typeId, keyword, current));
    }

    @Deprecated
    @ApiOperation("某个分类下分词词频")
    @GetMapping(value = "/getTagWordCount", produces = "application/json;charset=utf-8")
    public R<List<LowTagFrequencyVo>> getTagWordCount(
            @Validated @DecimalMax(value = "4", message = "一级标签类型不合法") @DecimalMin(value = "1", message = "一级标签类型不合法")
            @RequestParam Integer typeId) {
        return RUtil.success(blogService.getTagWordCount(typeId));
    }

    @Deprecated
    @ApiOperation("查询某个用户的帖子")
    @GetMapping(value = "/queryOnesBlog", produces = "application/json;charset=utf-8")
    public R<List<BlogVo>> queryOnesBlogs(@RequestParam(value = "current", defaultValue = "1") Integer current,
                                          @RequestParam Long userId) {
        return RUtil.success(blogService.getOnesBlogVo(userId, current));
    }
}

