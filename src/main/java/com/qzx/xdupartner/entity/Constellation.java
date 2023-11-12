package com.qzx.xdupartner.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

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
public class Constellation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 星座编号
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 星座名称
     */
    private String title;

    /**
     * 星座简介
     */
    private String description;


}
