package com.qzx.xdupartner.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author qzx
 * @since 2023-08-12
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class Blog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发帖用户id
     */
    private Long userId;

    /**
     * 是否匿名 0否1是
     */
    private Integer isAnonymous;

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
    private Integer highTagId;

    /**
     * 二级分类id,以&分割
     */
    private String lowTags;

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
     * 照片id，最多9张，多张以&隔开
     */
    private String images;

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
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


}
