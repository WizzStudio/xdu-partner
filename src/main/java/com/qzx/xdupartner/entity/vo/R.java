package com.qzx.xdupartner.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@ApiModel("统一返回体")
@Accessors(chain = true)
public class R<T> {
    // 状态码
    @ApiModelProperty("状态码")
    private int code;

    // 状态信息
    @ApiModelProperty("状态信息")
    private String msg;

    // 返回对象
    @ApiModelProperty("数据")
    private T data;

    // 手动设置返回vo
    public R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // 默认返回成功状态码，数据对象
    public R(T data) {
        this.code = ResultCode.SUCCESS.getCode();
        this.msg = ResultCode.SUCCESS.getMsg();
        this.data = data;
    }

    // 返回指定状态码，数据对象
    public R(StatusCode statusCode, T data) {
        this.code = statusCode.getCode();
        this.msg = statusCode.getMsg();
        this.data = data;
    }

    // 只返回状态码
    public R(StatusCode statusCode) {
        this.code = statusCode.getCode();
        this.msg = statusCode.getMsg();
        this.data = null;
    }

}
