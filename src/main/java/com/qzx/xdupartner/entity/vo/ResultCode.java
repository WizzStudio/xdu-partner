package com.qzx.xdupartner.entity.vo;

import lombok.Getter;

@Getter
public enum ResultCode implements StatusCode {
    SUCCESS(1000, "请求成功"),
    FAILED(2001, "请求失败"),
    VALIDATE_ERROR(2002, "请求参数有误"),
    UNKNOWN_ERROR(2003, "未知错误"),
    MAIL_CODE_ERROR(2004, "邮箱验证码错误"),
    WECHAT_ERROR(2005, "微信登录错误"),
    MAIL_SEND_ERROR(2006, "邮箱验证码发送失败"),
    RESPONSE_PACK_ERROR(2007, "response返回包装失败"),
    STU_ID_ERROR(2008,"学号格式错误");
    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}