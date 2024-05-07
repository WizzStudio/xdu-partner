package com.qzx.xdupartner.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
@ApiModel("返回状态结构体")
public enum ResultCode implements StatusCode {
    SUCCESS(1000, "请求成功"),
    FAILED(2001, "请求失败"),
    VALIDATE_ERROR(2002, "请求参数有误"),
    UNKNOWN_ERROR(2003, "未知错误"),
    MAIL_CODE_ERROR(2004, "邮箱验证码错误"),
    WECHAT_ERROR(2005, "微信登录错误"),
    MAIL_SEND_ERROR(2006, "邮箱验证码发送失败，请稍后再试"),
    RESPONSE_PACK_ERROR(2007, "response返回包装失败"),
    STU_ID_ERROR(2008, "学号格式错误"),
    HAS_VERIFIED_ERROR(2009, "账户已认证过，请勿重复认证哦"),
    PHONE_NO_WRONG_ERROR(2010, "手机号格式错误"),
    MESSAGE_SEND_ERROR(2011, "发送短信失败，请稍后再试"),
    MESSAGE_HAS_SENT_ERROR(2012, "短信5分钟内有效，请勿重复发送"), VERIFY_TOKEN_ERROR(2013, "token不存在");
    @ApiModelProperty("返回状态码")
    private final int code;
    @ApiModelProperty("信息-错误时用其内容")
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}