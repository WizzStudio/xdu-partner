package com.qzx.xdupartner.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import com.qzx.xdupartner.entity.dto.WxUserInfo;
import com.qzx.xdupartner.entity.vo.R;
import com.qzx.xdupartner.entity.vo.ResultCode;
import com.qzx.xdupartner.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/wx/user")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class WechatController {
    private final WxMaService wxMaService;

    private final UserInfoService userInfoService;

    /**
     * 登陆接口
     */
    @GetMapping("/login")
    public R<WxMaJscode2SessionResult> login(@RequestParam("code") String code) {
        return new R<>(ResultCode.SUCCESS, userInfoService.login(code));
    }

    /**
     * <pre>
     * 获取用户信息接口
     * </pre>
     */
    @PostMapping("/getUserInfo")
    public R<WxMaUserInfo> getUserInfo(@RequestBody WxUserInfo userInfo) {
        return new R<>(ResultCode.SUCCESS, userInfoService.getUserInfo(userInfo));
    }
}
