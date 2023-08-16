package com.qzx.xdupartner.entity.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.util.List;

@Data
public class BlogDto {
    /**
     * 标题
     */
    @NotBlank(message = "标题不应为空!")
    @Size(max = 30, min = 1, message = "标题字数不合法,应为1~30字")
    private String title;

    /**
     * 联系方式
     */
    @Size(max = 40, message = "联系方式字数不合法,应为0~40字")
    private String contact;

    /**
     * 一级分类
     */
    @NotNull(message = "分区不应为空!")
    @DecimalMax(value = "4", message = "分区应为1到4")
    @DecimalMin(value = "1", message = "分区应为1到4")
    private Integer highTagId;
    /**
     * 二级分类
     */
//    private String lowTags;
    private List<String> lowTags;
    /**
     * 具体时间
     */
    @Size(max = 30, message = "何时见面字数不合法,应为0~30字")
    private String whenMeet;

    /**
     * 具体地点
     */
    @Size(max = 30, message = "在哪见面字数不合法,应为0~30字")
    private String location;

    /**
     * 几缺几
     */
    @Size(max = 20, message = "几缺几字数不合法,应为0~20字")
    private String absent;

    /**
     * 照片aes加密后的字符串,解密id后拼一起
     */
    @NotNull(message = "图片列表可以提交空数组但不能不提交")
    private List<String> imageList;

    /**
     * 文字描述
     */
    @NotBlank(message = "内容不应为空!")
    @Size(max = 500, min = 1, message = "内容字数不合法,应为1~500字")
    private String content;
    /**
     * 是否匿名 0否1是
     */
    @DecimalMax(value = "1", message = "是否匿名应为0或1")
    @DecimalMin(value = "0", message = "是否匿名应为0或1")
    private Integer isAnonymous;
}
