package com.qzx.xdupartner.service;

import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import com.qzx.xdupartner.entity.dto.WxUserInfo;
import com.qzx.xdupartner.exception.MailCodeWrongException;
import me.chanjar.weixin.common.error.WxErrorException;

public interface UserInfoService {

    WxMaJscode2SessionResult register(String stuId, String code, String chsiCode) throws WxErrorException, MailCodeWrongException;

    WxMaJscode2SessionResult login(String code);

    WxMaUserInfo getUserInfo(WxUserInfo userInfo);
}


