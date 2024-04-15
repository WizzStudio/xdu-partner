package com.qzx.xdupartner.service;

import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import com.qzx.xdupartner.entity.Result;
import com.qzx.xdupartner.entity.dto.WxUserInfo;
import com.qzx.xdupartner.exception.MailCodeWrongException;
import me.chanjar.weixin.common.error.WxErrorException;

public interface UserInfoService {

    Result register(String stuId, String code, String verCode) throws WxErrorException,
            MailCodeWrongException;

    Result login(String code);

    WxMaUserInfo getUserInfo(WxUserInfo userInfo);
}


