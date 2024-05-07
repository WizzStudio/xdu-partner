package com.qzx.xdupartner.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.qzx.xdupartner.constant.RedisConstant;
import com.qzx.xdupartner.entity.User;
import com.qzx.xdupartner.entity.dto.PhoneAuthDto;
import com.qzx.xdupartner.entity.vo.LoginVo;
import com.qzx.xdupartner.entity.vo.ResultCode;
import com.qzx.xdupartner.exception.APIException;
import com.qzx.xdupartner.service.MsmService;
import com.qzx.xdupartner.service.PhoneService;
import com.qzx.xdupartner.service.UserService;
import com.qzx.xdupartner.util.UserUtil;
import com.qzx.xdupartner.util.VerifyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class PhoneServiceImpl implements PhoneService {
    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Resource
    UserService userService;
    @Resource
    MsmService msmService;

    @Override
    public boolean checkSent(String phone) {
        String val = stringRedisTemplate.opsForValue().get(RedisConstant.PHONE_LOGIN_PREFIX + phone);
        return StrUtil.isBlank(val);
    }

    @Override
    public boolean sendVerCode(String phone) {
        String verCode = VerifyUtil.getVerCode();
        boolean sent = msmService.send(phone, verCode);
        log.info("send tencent phone msg: phone:[{}] vcode:[{}]", phone, verCode);
        if (!sent) {
            throw new APIException(ResultCode.MESSAGE_SEND_ERROR);
        }
        stringRedisTemplate.opsForValue().set(RedisConstant.PHONE_LOGIN_PREFIX + phone, verCode, 5, TimeUnit.MINUTES);
        return true;
    }

    @Override
    public LoginVo verifyVerCode(PhoneAuthDto phoneAuthDto) {
        String key = RedisConstant.PHONE_LOGIN_PREFIX + phoneAuthDto.getPhone();
        String vcode =
                stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(vcode) || !vcode.equals(phoneAuthDto.getVerCode())) {
            throw new APIException(ResultCode.PHONE_VERIFY_ERROR);
        }
        Boolean deleted = stringRedisTemplate.delete(key);
        if (!BooleanUtil.isTrue(deleted)) {
            throw new APIException(ResultCode.FAILED);
        }
        //查看库中是否存在手机号
        User user = userService.lambdaQuery().eq(User::getPhone, phoneAuthDto.getPhone()).one();
        //不存在则创建账号
        if (ObjectUtil.isNotNull(user)) {
            user = UserUtil.createUser(user.getPhone(), StrUtil.EMPTY);
        }
        boolean saved = userService.save(user);
        if (!saved) {
            throw new APIException(ResultCode.FAILED);
        }
        //返回LoginVo
        //todo 构造token
        return new LoginVo().setUserInfoVo(UserUtil.convertToUserInfoVo(user)).setToken("");
    }
}
