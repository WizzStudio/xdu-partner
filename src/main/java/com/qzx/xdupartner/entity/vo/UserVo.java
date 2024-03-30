package com.qzx.xdupartner.entity.vo;

import cn.hutool.core.util.RandomUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qzx.xdupartner.constant.SystemConstant;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserVo {
    private Long id;
    private String icon;
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
