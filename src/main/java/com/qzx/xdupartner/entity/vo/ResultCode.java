package com.qzx.xdupartner.entity.vo;

import lombok.Getter;

@Getter
public enum ResultCode implements StatusCode {
    SUCCESS(1000, "请求成功"),
    FAILED(2001, "请求失败"),
    VALIDATE_ERROR(2002, "请求参数有误"),
    UNKNOWN_ERROR(2003, "未知错误"),
    RESPONSE_PACK_ERROR(2003, "response返回包装失败");

    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}