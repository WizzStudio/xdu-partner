package com.qzx.xdupartner.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel("登录接口展示")
public class LoginVo {
    @ApiModelProperty("用户信息")
    UserInfoVo userInfoVo;
    @ApiModelProperty("放置在header中的token，用于身份验证")
    String token;
}
