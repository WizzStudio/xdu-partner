package com.qzx.xdupartner.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
@ApiModel("帖子")
public class BlogVo {
    /**
     * 主键
     */
    @ApiModelProperty("帖子id")
    private Long id;

    /**
     * 发帖用户
     */
    @ApiModelProperty("发帖用户")
    private UserVo userVo;

    /**
     * 是否被当前用户点赞
     */
    @ApiModelProperty("是否被当前用户点赞")
    private Boolean isLiked;

    /**
     * 标题
     */
    @ApiModelProperty("帖子标题")
    private String title;

    /**
     * 联系方式
     */
    @ApiModelProperty("联系方式")
    private String contact;

    /**
     * 一级分类
     */
    @ApiModelProperty("一级分类")
    private int highTag;
    @ApiModelProperty("一级分类名称")
    private String highTagValue;
    /**
     * 二级分类
     */
    @ApiModelProperty("二级分类")
    private List<String> lowTags;
    /**
     * 具体时间
     */
    @ApiModelProperty("具体时间")
    private String whenMeet;

    /**
     * 具体地点
     */
    @ApiModelProperty("具体地点")
    private String location;

    /**
     * 几缺几
     */
    @ApiModelProperty("几缺几")

    private String absent;

    /**
     * 浏览次数
     */
    @ApiModelProperty("浏览次数")

    private Integer viewTimes;

    /**
     * 照片，最多9张
     */
    @ApiModelProperty("图片url")
    private List<String> images;

    /**
     * 文字描述
     */
    @ApiModelProperty("文字描述")
    private String content;

    /**
     * 点赞数量
     */
    @ApiModelProperty("点赞数量")
    private Integer liked;

    /**
     * 是否找到搭子0否1是
     */
    @ApiModelProperty("是否找到搭子")
    private Integer isComplete;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    /**
     * 更改时间
     */
    @ApiModelProperty("更改时间")
    private LocalDateTime updateTime;

}
