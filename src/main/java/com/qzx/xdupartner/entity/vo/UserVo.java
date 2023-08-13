package com.qzx.xdupartner.entity.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qzx.xdupartner.constant.RedisConstant;
import com.qzx.xdupartner.constant.SystemConstant;

import cn.hutool.core.util.RandomUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserVo {
    private Long id;

    public UserVo(String icon, String nickName) {
        this.icon = icon;
        this.nickName = nickName;
    }
    private String icon;
    private String nickName;
    @JsonIgnore
    public static UserVo getAnonymousVo(){
        UserVo userVo = new UserVo();
        userVo.setId(0L);
        userVo.setIcon(String.valueOf(RandomUtil.randomInt(SystemConstant.RANDOM_ICON_MIN,SystemConstant.RANDOM_ICON_MAX)));
        userVo.setNickName("匿名用户");
        return userVo;
    }
}
