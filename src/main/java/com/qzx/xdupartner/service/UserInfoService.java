package com.qzx.xdupartner.service;

import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import com.qzx.xdupartner.entity.dto.WxUserInfo;

public interface UserInfoService {

    /**
     * 登录
     *
     * @param code code
     * @param chsiCode 学信网认证码
     * @return WxMaJscode2SessionResult
     */
    WxMaJscode2SessionResult login(String code,String chsiCode);

    /**
     * 获取用户信息
     *
     * @param userInfo 包含一些加密的信息
     * @return WxMaUserInfo
     */
    WxMaUserInfo getUserInfo(WxUserInfo userInfo);
}


