package com.qzx.xdupartner.controller;

import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import cn.hutool.core.util.ObjectUtil;
import com.qzx.xdupartner.entity.Result;
import com.qzx.xdupartner.entity.dto.WxPhoneAuthDto;
import com.qzx.xdupartner.entity.dto.WxUserInfo;
import com.qzx.xdupartner.entity.vo.LoginVo;
import com.qzx.xdupartner.entity.vo.R;
import com.qzx.xdupartner.entity.vo.ResultCode;
import com.qzx.xdupartner.exception.MailCodeWrongException;
import com.qzx.xdupartner.service.UserInfoService;
import com.qzx.xdupartner.service.WeChatService;
import com.qzx.xdupartner.util.RUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@Api
@Slf4j
@RestController
@RequestMapping("/wx/user")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class WechatController {

    private final WeChatService weChatService;
    private final UserInfoService userInfoService;

    /**
     * 注册接口
     */
    @Deprecated
    @ApiOperation("")
    @GetMapping("/register")
    public R<Result> registor(
            @RequestParam("stuId") String stuId,
            @RequestParam("code") String code,
            @RequestParam("verCode") String verCode) {

        Result register = null;
        try {
            register = userInfoService.register(stuId, code, verCode);
        } catch (WxErrorException e) {
            return new R<>(ResultCode.WECHAT_ERROR, null);
        } catch (MailCodeWrongException e) {
            return new R<>(ResultCode.MAIL_CODE_ERROR, null);
        }

        return RUtil.success(register);
    }

    /**
     * login接口
     */
    @Deprecated
    @ApiOperation("")
    @GetMapping("/login")
    public R<Result> login(@RequestParam("code") String code) {
        Result login = userInfoService.login(code);
        if (ObjectUtil.isNull(login)) {
            return new R<>(ResultCode.FAILED, null);
        }
        return RUtil.success(login);
    }


    @ApiOperation("")
    @PostMapping("/login/phone")
    public R<LoginVo> login(@Validated @RequestBody @NotNull WxPhoneAuthDto wxPhoneAuthDto) {
        return RUtil.success(weChatService.loginByPhoneCode(wxPhoneAuthDto));
    }

    /**
     * <pre>
     * 获取用户信息接口
     * </pre>
     */
    @Deprecated
    @ApiOperation("")
    @PostMapping("/getUserInfo")
    public R<WxMaUserInfo> getUserInfo(@RequestBody WxUserInfo userInfo) {
        return RUtil.success(userInfoService.getUserInfo(userInfo));
    }
}

