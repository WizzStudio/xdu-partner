package com.qzx.xdupartner.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BlogVo {
    /**
     * 主键
     */
    private Long id;

    /**
     * 发帖用户
     */
    private UserVo userVo;

    /**
     * 是否被当前用户点赞
     */
    private Boolean isLiked;

    /**
     * 标题
     */
    private String title;

    /**
     * 联系方式
     */
    private String contact;

    /**
     * 一级分类
     */
    private int highTag;
    /**
     * 二级分类
     */
    private List<String> lowTags;
    /**
     * 具体时间
     */
    private String whenMeet;

    /**
     * 具体地点
     */
    private String location;

    /**
     * 几缺几
     */
    private String absent;

    /**
     * 浏览次数
     */
    private Integer viewTimes;

    /**
     * 照片，最多9张，多张以&隔开
     */
    private List<String> images;

    /**
     * 文字描述
     */
    private String content;

    /**
     * 点赞数量
     */
    private Integer liked;

    /**
     * 是否找到搭子0否1是
     */
    private Integer isComplete;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更改时间
     */
    private LocalDateTime updateTime;

}
