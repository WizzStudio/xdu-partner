package com.qzx.xdupartner.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.qzx.xdupartner.constant.RedisConstant;
import com.qzx.xdupartner.entity.User;
import com.qzx.xdupartner.entity.vo.R;
import com.qzx.xdupartner.entity.vo.ResultCode;
import com.qzx.xdupartner.service.MailService;
import com.qzx.xdupartner.service.UserService;
import com.qzx.xdupartner.util.VerCodeGenerateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Api
@Slf4j
@RestController
@RequestMapping("/verify")
public class VerifyController {
    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Resource
    UserService userService;
    @Resource
    MailService mailService;

    @ApiOperation("")
    @GetMapping("/sendCode")
    public R<String> sendCode(@RequestParam("stuId") String stuId) {
        if (!(StrUtil.isNumeric(stuId) && stuId.length() == 11)) {
            return new R<>(ResultCode.STU_ID_ERROR);
        }
        String verCode = VerCodeGenerateUtil.getVerCode();
        boolean isSend = mailService.sendMail(stuId, verCode);
        if (isSend) {
            //发送验证码成功
            stringRedisTemplate.opsForValue().set(RedisConstant.MAIL_CODE_PREFIX + stuId, verCode, 10,
                    TimeUnit.MINUTES);
            return new R<>(ResultCode.SUCCESS, "发送成功");
        }
        return new R<>(ResultCode.MAIL_SEND_ERROR);
    }

    @ApiOperation("")
    @GetMapping("/testLogin")
    public R<String> testLogin(@RequestParam("stuId") String stuId) {
        String sessionKey1 = "12345678";
        String sessionKey2 = "123456789";
        if (stuId.equals(sessionKey1)) {
            User user = userService.lambdaQuery().eq(User::getId, 35).one();
            return getStringR(sessionKey1, user);
        } else if (stuId.equals(sessionKey2)) {
            User user = userService.lambdaQuery().eq(User::getId, 14).one();
            return getStringR(sessionKey2, user);
        }
        return new R<>(ResultCode.FAILED, "");
    }

    private R<String> getStringR(String sessionKey, User user) {
        if (ObjectUtil.isNull(user)) {
            return null;
        }
        user.setSessionKey(sessionKey);
        stringRedisTemplate.opsForValue().set(RedisConstant.LOGIN_PREFIX + sessionKey, JSONUtil.toJsonStr(user),
                RedisConstant.LOGIN_VALID_TTL, TimeUnit.DAYS);
        return new R<>(ResultCode.SUCCESS, sessionKey);
    }

    public static void main(String[] args) {
        if ((StrUtil.isNumeric("21009200334") && "21009200334".length() == 11)) {
            System.out.println("23444");
        }
    }
}
