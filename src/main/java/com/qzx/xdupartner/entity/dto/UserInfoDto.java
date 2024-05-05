package com.qzx.xdupartner.entity.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class UserInfoDto {

    /**
     * 昵称
     */
    @Size(min = 1, max = 15, message = "昵称长度为1~15个字符")
    private String nickName;
    @Size(min = 1, max = 15, message = "专业名称长度为1~15个字符")
    private String majorName;

    /**
     * 星座编号,具体星座在星座表里找
     */
    @DecimalMin(value = "1")
    @DecimalMax(value = "12")
    private Integer constellation;

    /**
     * 照片墙 最多三张 照片id 用_picture_分割
     */
    private List<String> picture;
    /**
     * qq号
     */
    @Size(max = 20)
    private String qq;

    /**
     * 头像
     */
    private String icon;

    /**
     * 200字以内描述自己
     */
    @Size(max = 200)
    private String myDescription;

    /**
     * 一级标签
     */
    @DecimalMin(value = "1")
    @DecimalMax(value = "4")
    private Integer highTag;

    /**
     * MBTI性格测试结果编号,16种具体在mbti表里
     */
    @DecimalMin(value = "1")
    @DecimalMax(value = "16")
    private Integer mbti;
}
