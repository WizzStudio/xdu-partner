package com.qzx.xdupartner.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import cn.binarywang.wx.miniapp.util.WxMaConfigHolder;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.qzx.xdupartner.constant.RedisConstant;
import com.qzx.xdupartner.entity.Result;
import com.qzx.xdupartner.entity.User;
import com.qzx.xdupartner.entity.dto.WxUserInfo;
import com.qzx.xdupartner.exception.MailCodeWrongException;
import com.qzx.xdupartner.service.UserInfoService;
import com.qzx.xdupartner.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author 成大事
 * @since 2022/7/27 22:48
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserInfoServiceImpl implements UserInfoService {

    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private final WxMaService wxMaService;
    private final UserService userService;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Result register(String stuId, String code, String verCode) throws WxErrorException,
            MailCodeWrongException {
        //验证邮箱
        String realCode = stringRedisTemplate.opsForValue().get(RedisConstant.MAIL_CODE_PREFIX + stuId);
        if (!verCode.equals(realCode)) {
            throw new MailCodeWrongException();
        }
        WxMaJscode2SessionResult session;
        try {
            session = wxMaService.getUserService().getSessionInfo(code);
            log.info(session.getSessionKey());
            log.info(session.getOpenid());
        } finally {
            WxMaConfigHolder.remove();//清理ThreadLocal
        }
        //TODO 可以增加自己的逻辑，关联业务相关数据
        User user = userService.insertNewUser(session.getOpenid(), stuId);
        user.setSessionKey(session.getSessionKey());
        stringRedisTemplate.opsForValue().set(RedisConstant.LOGIN_PREFIX + session.getSessionKey(),
                JSONUtil.toJsonStr(user), RedisConstant.LOGIN_VALID_TTL, TimeUnit.DAYS);
        return new Result(session, user.getId());
    }

    @Override
    public Result login(String code) {
        try {
            WxMaJscode2SessionResult session = wxMaService.getUserService().getSessionInfo(code);
            log.info(session.getSessionKey());
            log.info(session.getOpenid());
            //TODO 可以增加自己的逻辑，关联业务相关数据
            User user = userService.lambdaQuery().eq(User::getOpenId, session.getOpenid()).one();
            if (ObjectUtil.isNull(user)) {
                return null;
            }
            user.setSessionKey(session.getSessionKey());
            stringRedisTemplate.opsForValue().set(RedisConstant.LOGIN_PREFIX + session.getSessionKey(),
                    JSONUtil.toJsonStr(user),
                    RedisConstant.LOGIN_VALID_TTL,
                    TimeUnit.DAYS);
            return new Result(session, user.getId());
        } catch (WxErrorException e) {
            log.error(e.getMessage(), e);
            return null;
        } finally {
            WxMaConfigHolder.remove();//清理ThreadLocal
        }
    }

    @Override
    public WxMaUserInfo getUserInfo(WxUserInfo userInfo) {

        // 用户信息校验
        if (!wxMaService.getUserService().checkUserInfo(userInfo.getSessionKey(), userInfo.getRawData(),
                userInfo.getSignature())) {
            WxMaConfigHolder.remove();//清理ThreadLocal
            return null;
        }

        // 解密用户信息
        WxMaUserInfo wxMaUserInfo = wxMaService.getUserService().getUserInfo(userInfo.getSessionKey(),
                userInfo.getEncryptedData(), userInfo.getIv());
        WxMaConfigHolder.remove();//清理ThreadLocal
        return wxMaUserInfo;
    }
}
