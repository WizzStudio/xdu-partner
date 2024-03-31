package com.qzx.xdupartner.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import cn.binarywang.wx.miniapp.util.WxMaConfigHolder;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.qzx.xdupartner.constant.RedisConstant;
import com.qzx.xdupartner.entity.User;
import com.qzx.xdupartner.entity.dto.SchoolInfoDto;
import com.qzx.xdupartner.entity.dto.WxUserInfo;
import com.qzx.xdupartner.service.UserInfoService;
import com.qzx.xdupartner.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

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
    public WxMaJscode2SessionResult register(String code, String chsiCode) {
        Future<SchoolInfoDto> schoolInfoDtoFuture = null;
//        executor.submit(() -> VerifyUtil.visitData(chsiCode));
        AtomicReference<WxMaJscode2SessionResult> session = new AtomicReference<>();
        Future<AtomicReference<WxMaJscode2SessionResult>> sessionResultFuture = executor.submit(() -> {
            try {
                session.set(wxMaService.getUserService().getSessionInfo(code));
                log.info(session.get().getSessionKey());
                log.info(session.get().getOpenid());
                return session;
            } catch (WxErrorException e) {
                log.error(e.getMessage(), e);
                return null;
            } finally {
                WxMaConfigHolder.remove();//清理ThreadLocal
            }
        });
        SchoolInfoDto schoolInfoDto;
        WxMaJscode2SessionResult wxMaJscode2SessionResult;
        try {
            schoolInfoDto = schoolInfoDtoFuture.get(5, TimeUnit.SECONDS);
            wxMaJscode2SessionResult = sessionResultFuture.get(5, TimeUnit.SECONDS).get();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        if (schoolInfoDto == null || wxMaJscode2SessionResult == null) {
            return null;
        }
        //TODO 可以增加自己的逻辑，关联业务相关数据
        User user = userService.insertNewUser(wxMaJscode2SessionResult.getOpenid(), schoolInfoDto.getStuId());
        user.setSessionKey(session.get().getSessionKey());
        stringRedisTemplate.opsForValue().set(RedisConstant.LOGIN_PREFIX + session.get().getSessionKey(),
                JSONUtil.toJsonStr(user),
                RedisConstant.LOGIN_VALID_TTL,
                TimeUnit.DAYS);
        return wxMaJscode2SessionResult;
    }

    @Override
    public WxMaJscode2SessionResult login(String code) {
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
            User finalUser = user;
            stringRedisTemplate.opsForValue().set(RedisConstant.LOGIN_PREFIX + session.getSessionKey(),
                    JSONUtil.toJsonStr(finalUser),
                    RedisConstant.LOGIN_VALID_TTL,
                    TimeUnit.DAYS);
            return session;
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
