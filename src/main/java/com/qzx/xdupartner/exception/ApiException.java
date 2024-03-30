package com.qzx.xdupartner.exception;


import com.qzx.xdupartner.entity.vo.ResultCode;
import com.qzx.xdupartner.entity.vo.StatusCode;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final int code;
    private final String msg;

    // 手动设置异常
    public ApiException(StatusCode statusCode, String message) {
        // message用于用户设置抛出错误详情，例如：当前价格-5，小于0
        super(message);
        // 状态码
        this.code = statusCode.getCode();
        // 状态码配套的msg
        this.msg = statusCode.getMsg();
    }

    // 默认异常使用APP_ERROR状态码
    public ApiException(String message) {
        super(message);
        this.code = ResultCode.FAILED.getCode();
        this.msg = ResultCode.FAILED.getMsg();
    }

}
