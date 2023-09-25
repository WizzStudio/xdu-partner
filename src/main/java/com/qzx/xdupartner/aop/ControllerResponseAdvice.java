package com.qzx.xdupartner.aop;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qzx.xdupartner.entity.vo.ResultCode;
import com.qzx.xdupartner.entity.vo.ResultVo;
import com.qzx.xdupartner.exception.ApiException;


@RestControllerAdvice(basePackages = {"com.qzx.xdupartner"})
public class ControllerResponseAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        return !methodParameter.getParameterType().isAssignableFrom(ResultVo.class);
    }

    @Override
    public Object beforeBodyWrite(Object data, MethodParameter returnType, MediaType mediaType, Class<?
            extends HttpMessageConverter<?>> aClass, ServerHttpRequest request, ServerHttpResponse response) {
        //String类型不能直接包装
        if (returnType.getGenericParameterType().equals(String.class)) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                // 将数据包装在ResultVo里后转换为json串进行返回
                return objectMapper.writeValueAsString(new ResultVo(data));
            } catch (JsonProcessingException e) {
                throw new ApiException(ResultCode.RESPONSE_PACK_ERROR, e.getMessage());
            }
        }
        // 否则直接包装成ResultVo返回
        return new ResultVo(data);
    }
}
