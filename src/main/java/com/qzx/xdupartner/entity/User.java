package com.qzx.xdupartner.entity;

import com.baomidou.mybatisplus.annotation.*;
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
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 专业名称
     */
    private String majorName;

    /**
     * 学号
     */
    private String stuId;

    /**
     * 0否1是管理员
     */
    private Integer isAdmin;

    /**
     * qq号
     */
    private String qq;

    /**
     * 头像
     */
    private String icon;

    /**
     * 照片墙 最多三张 照片id 用_picture_分割
     */
    private String picture;

    /**
     * 200字以内描述自己
     */
    private String myDescription;

    /**
     * openid
     */
    private String openId;

    /**
     * sessionKey
     */
    @TableField(exist = false)
    private String sessionKey;

    /**
     * 需求倾向
     */
    private Integer highTag;

    /**
     * MBTI性格测试结果编号,16种具体在mbti表里
     */
    private Integer mbti;

    /**
     * 星座编号,具体星座在星座表里找
     */
    private Integer constellation;

    /**
     * 创建时间
     */
    private Date creatTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


    /**
     * 是否删除
     */
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;
    /**
     * 手机号
     */
    private String phone;

}
