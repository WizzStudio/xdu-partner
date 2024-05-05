package com.qzx.xdupartner.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ManualVerifyDto {
    @NotBlank(message = "学号不能为空")
    String stuId;
    @NotBlank(message = "学信网在线验证码不能为空")
    String onlineVerCode;
    String remark;
}
