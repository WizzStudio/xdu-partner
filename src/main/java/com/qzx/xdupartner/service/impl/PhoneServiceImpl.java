package com.qzx.xdupartner.service.impl;

import cn.hutool.core.util.StrUtil;
import com.qzx.xdupartner.constant.RedisConstant;
import com.qzx.xdupartner.service.PhoneService;
import com.qzx.xdupartner.util.VerCodeGenerateUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class PhoneServiceImpl implements PhoneService {
    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean checkSent(String phone) {
        String val = stringRedisTemplate.opsForValue().get(RedisConstant.PHONE_LOGIN_PREFIX + phone);
        return StrUtil.isBlank(val);
    }

    @Override
    public boolean sendVerCode(String phone) {
        String verCode = VerCodeGenerateUtil.getVerCode();
        //todo 发送短信

        stringRedisTemplate.opsForValue().set(RedisConstant.PHONE_LOGIN_PREFIX + phone, verCode, 5, TimeUnit.MINUTES);
        return true;
    }
}
