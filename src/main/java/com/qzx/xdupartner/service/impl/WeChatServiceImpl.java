package com.qzx.xdupartner.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.WxMaUserService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.qzx.xdupartner.entity.User;
import com.qzx.xdupartner.entity.dto.WxPhoneAuthDto;
import com.qzx.xdupartner.entity.vo.LoginVo;
import com.qzx.xdupartner.entity.vo.ResultCode;
import com.qzx.xdupartner.exception.APIException;
import com.qzx.xdupartner.service.UserService;
import com.qzx.xdupartner.service.WeChatService;
import com.qzx.xdupartner.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class WeChatServiceImpl implements WeChatService {
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private final WxMaService wxMaService;
    private final UserService userService;

    @Override
    public LoginVo loginByPhoneCode(WxPhoneAuthDto wxPhoneAuthDto) throws APIException {
        //微信登录流程
        WxMaUserService wxMaUserService = wxMaService.getUserService();
        Future<WxMaPhoneNumberInfo> phoneNoFuture =
                executorService.submit(() -> wxMaUserService.getPhoneNoInfo(wxPhoneAuthDto.getPhoneAuthCode()));
        Future<WxMaJscode2SessionResult> sessionFuture =
                executorService.submit(() -> wxMaUserService.getSessionInfo(wxPhoneAuthDto.getLoginCode()));
        WxMaPhoneNumberInfo wxMaPhoneNumberInfo;
        WxMaJscode2SessionResult sessionResult;
        try {
            wxMaPhoneNumberInfo = phoneNoFuture.get(5, TimeUnit.SECONDS);
            sessionResult = sessionFuture.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("wx login error. phoneAuthCode:[{}] loginCode:[{}] error:", wxPhoneAuthDto.getPhoneAuthCode(),
                    wxPhoneAuthDto.getLoginCode(), e);
            throw new APIException(ResultCode.WECHAT_ERROR);
        }
        User user =
                userService.lambdaQuery().eq(User::getPhone, wxMaPhoneNumberInfo.getPurePhoneNumber()).or().eq(User::getOpenId, sessionResult.getOpenid()).one();
        if (ObjectUtil.isNull(user)) {
            user = UserUtil.createUser(wxMaPhoneNumberInfo.getPurePhoneNumber(), sessionResult.getOpenid());
            userService.save(user);
        }
        if (StrUtil.isBlank(user.getPhone()) || StrUtil.isBlank(user.getOpenId())) {
            userService.save(user);
        }
        //todo 生成token
        String token = "";
        UserUtil.setRedisData(user, token);
        return new LoginVo().setUserInfoVo(UserUtil.convertToUserInfoVo(user)).setToken(token);
    }

    @Override
    public LoginVo loginByOpenid(String loginCode) throws APIException {
        return null;
    }
}
