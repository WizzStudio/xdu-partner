package com.qzx.xdupartner.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class WxPhoneAuthDto {
    @NotBlank
    String phoneAuthCode;
    @NotBlank
    String loginCode;
}
