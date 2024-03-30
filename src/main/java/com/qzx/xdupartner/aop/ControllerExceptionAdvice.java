package com.qzx.xdupartner.aop;

import cn.hutool.core.util.StrUtil;
import com.qzx.xdupartner.entity.vo.R;
import com.qzx.xdupartner.entity.vo.ResultCode;
import com.qzx.xdupartner.exception.ApiException;
import com.qzx.xdupartner.exception.ParamErrorException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class ControllerExceptionAdvice {
    @ExceptionHandler({BindException.class})
    public R<String> MethodArgumentNotValidExceptionHandler(BindException e) {
        // 从异常对象中拿到ObjectError对象
        ObjectError objectError = e.getBindingResult().getAllErrors().get(0);
        return new R(ResultCode.VALIDATE_ERROR, objectError.getDefaultMessage());
    }

    /**
     * 忽略参数异常处理器
     *
     * @param e 忽略参数异常
     * @return ResultVo
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public R<String> parameterMissingExceptionHandler(MissingServletRequestParameterException e) {
        log.error("", e);
        return new R(ResultCode.VALIDATE_ERROR, "请求参数 " + e.getParameterName() + " 不能为空");
    }

    /**
     * 缺少请求体异常处理器
     *
     * @param e 缺少请求体异常
     * @return ResultVo
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<String> parameterBodyMissingExceptionHandler(HttpMessageNotReadableException e) {
        log.error("", e);
        return new R(ResultCode.VALIDATE_ERROR, "参数体不能为空");
    }

    /**
     * 参数效验异常处理器
     *
     * @param e 参数验证异常
     * @return ResponseInfo
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<String> parameterExceptionHandler(MethodArgumentNotValidException e) {
        log.error("", e);
        // 获取异常信息
        BindingResult exceptions = e.getBindingResult();
        // 判断异常中是否有错误信息，如果存在就使用异常中的消息，否则使用默认消息
        if (exceptions.hasErrors()) {
            List<ObjectError> errors = exceptions.getAllErrors();
            if (!errors.isEmpty()) {
                // 这里列出了全部错误参数，按正常逻辑，只需要第一条错误即可
                FieldError fieldError = (FieldError) errors.get(0);
                return new R(ResultCode.VALIDATE_ERROR, fieldError.getDefaultMessage());
            }
        }
        return new R(ResultCode.VALIDATE_ERROR);
    }

    /**
     * 自定义参数错误异常处理器
     *
     * @param e 自定义参数
     * @return ResponseInfo
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ParamErrorException.class})
    public R<String> paramExceptionHandler(ParamErrorException e) {
        log.error("", e);
        // 判断异常中是否有错误信息，如果存在就使用异常中的消息，否则使用默认消息
        if (!StringUtils.isEmpty(e.getMessage())) {
            return new R(ResultCode.VALIDATE_ERROR, e.getMessage());
        }
        return new R(ResultCode.VALIDATE_ERROR);
    }

    @ExceptionHandler(ApiException.class)
    public R<String> APIExceptionHandler(ApiException e) {
        log.error(e.getMessage(), e);
        return new R(e.getCode(), e.getMessage(), null);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RuntimeException.class)
    public R<String> RuntimeExceptionHandler(RuntimeException e) {
        log.error(e.getMessage(), e);
        return new R(ResultCode.FAILED, e.getMessage());
    }


    @ExceptionHandler(value = {JwtException.class})
    public R<String> JWTExceptionHandler(JwtException e) {
        log.error(StrUtil.isBlank(e.getMessage()) ? e.getMessage() : "登录出错请重新登陆", e);
        return new R(ResultCode.FAILED, e.getMessage());
    }
}
