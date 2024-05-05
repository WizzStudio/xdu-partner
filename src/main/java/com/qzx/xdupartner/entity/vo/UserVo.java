package com.qzx.xdupartner.entity.vo;

import cn.hutool.core.util.RandomUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qzx.xdupartner.constant.SystemConstant;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel("简单用户展示")
public class UserVo {
    @ApiModelProperty("用户id")
    private Long id;
    @ApiModelProperty("用户头像")
    private String icon;
    @ApiModelProperty("昵称")
    private String nickName;

    public UserVo(String icon, String nickName) {
        this.icon = icon;
        this.nickName = nickName;
    }

    @JsonIgnore
    public static UserVo getAnonymousVo() {
        UserVo userVo = new UserVo();
        userVo.setId(0L);
        userVo.setIcon(SystemConstant.DEFAULT_ICON_URL + RandomUtil.randomInt(SystemConstant.RANDOM_ICON_MIN,
                SystemConstant.RANDOM_ICON_MAX) +
                ".jpg");
        userVo.setNickName("匿名用户");
        return userVo;
    }
}
