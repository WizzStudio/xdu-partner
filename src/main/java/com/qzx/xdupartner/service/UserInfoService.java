package com.qzx.xdupartner.service;

import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import com.qzx.xdupartner.entity.dto.WxUserInfo;

public interface UserInfoService {

    WxMaJscode2SessionResult register(String code, String chsiCode);

    WxMaJscode2SessionResult login(String code);

    WxMaUserInfo getUserInfo(WxUserInfo userInfo);
}


