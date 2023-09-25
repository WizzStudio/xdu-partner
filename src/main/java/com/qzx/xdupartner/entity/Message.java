package com.qzx.xdupartner.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 消息体
     */
    private String content;

    /**
     * 发送者id
     */
    private Long fromId;

    /**
     * 接收者id
     */
    private Long toId;

    /**
     * 消息类型
     */
    private Integer type;

    /**
     * 消息是否被接收 0否 1是
     */
    private Integer isReceived;

    /**
     * 发送时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;


}
