package com.qzx.xdupartner.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class ManualVerifyDto {
    @NotBlank(message = "学号不能为空")
    @ApiModelProperty("学号")
    String stuId;
    @NotBlank(message = "学信网在线验证码不能为空")
    @ApiModelProperty("学信网在线验证码")
    String onlineVerCode;
    @ApiModelProperty("备注")
    String remark;
    @ApiModelProperty("留下的邮箱，方便审核完后告知")
    String leftEmail;
}
