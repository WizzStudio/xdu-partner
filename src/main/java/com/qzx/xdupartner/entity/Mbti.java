package com.qzx.xdupartner.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *
 * </p>
 *
 * @author qzx
 * @since 2023-08-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Mbti implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * mbti编号
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * mbti名称
     */
    private String title;

    /**
     * mbti简介
     */
    private String description;


}
