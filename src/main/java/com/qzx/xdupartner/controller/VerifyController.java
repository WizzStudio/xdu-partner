package com.qzx.xdupartner.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.qzx.xdupartner.constant.RedisConstant;
import com.qzx.xdupartner.entity.User;
import com.qzx.xdupartner.entity.dto.ManualVerifyDto;
import com.qzx.xdupartner.entity.dto.PhoneAuthDto;
import com.qzx.xdupartner.entity.vo.LoginVo;
import com.qzx.xdupartner.entity.vo.R;
import com.qzx.xdupartner.entity.vo.ResultCode;
import com.qzx.xdupartner.exception.APIException;
import com.qzx.xdupartner.service.MailService;
import com.qzx.xdupartner.service.PhoneService;
import com.qzx.xdupartner.service.UserService;
import com.qzx.xdupartner.util.RUtil;
import com.qzx.xdupartner.util.UserHolder;
import com.qzx.xdupartner.util.VerifyUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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

    @Resource
    PhoneService phoneService;

    @Deprecated
    @ApiOperation("")
    @GetMapping("/sendCode")
    public R<String> sendCode(@RequestParam("stuId") String stuId) {
        if (!(StrUtil.isNumeric(stuId) && stuId.length() == 11)) {
            return new R<>(ResultCode.STU_ID_ERROR);
        }
        String verCode = VerifyUtil.getVerCode();
        boolean isSend = mailService.sendVerifyEmail(stuId, verCode);
        if (isSend) {
            //发送验证码成功
            stringRedisTemplate.opsForValue().set(RedisConstant.MAIL_CODE_PREFIX + stuId, verCode, 10,
                    TimeUnit.MINUTES);
            return RUtil.success("发送成功");
        }
        return new R<>(ResultCode.MAIL_SEND_ERROR);
    }

    @Deprecated
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
        return RUtil.success(sessionKey);
    }

    @ApiOperation("")
    @GetMapping("/email/send")
    public R<String> sendEmailCode(@RequestParam String stuId) {
        if (!(StrUtil.isNumeric(stuId) && stuId.length() == 11)) {
            return new R<>(ResultCode.STU_ID_ERROR);
        }
        if (userService.checkUserIsVerified(UserHolder.getUserId())) {
            return RUtil.error(ResultCode.HAS_VERIFIED_ERROR);
        }
        String verCode = VerifyUtil.getVerCode();
        return mailService.sendVerifyEmail(stuId, verCode) ? RUtil.success("发送成功，注意查收学校邮箱邮件") :
                RUtil.error(ResultCode.MAIL_SEND_ERROR);
    }

    @ApiOperation("")
    @GetMapping("/phone/send")
    public R<String> sendPhoneVerCode(@RequestParam String phone) {
        if (!PhoneUtil.isPhone(phone)) {
            return RUtil.error(ResultCode.PHONE_NO_WRONG_ERROR);
        }
        if (phoneService.checkSent(phone)) {
            return RUtil.error(ResultCode.MESSAGE_HAS_SENT_ERROR);
        }
        return phoneService.sendVerCode(phone) ? RUtil.success("发送成功，注意查收短信") :
                RUtil.error(ResultCode.MESSAGE_SEND_ERROR);
    }

    @ApiOperation("")
    @PostMapping("/phone/login")
    public R<LoginVo> verifyPhoneVerCode(@RequestBody PhoneAuthDto phoneAuthDto) {
        return null;
    }

    @ApiOperation("")
    @PostMapping("/manual")
    public R<String> manualVerify(@Validated @RequestBody ManualVerifyDto manualVerifyDto) {
//        if (userService.checkUserIsVerified(UserHolder.getUserId())) {
//            return RUtil.error(ResultCode.HAS_VERIFIED_ERROR);
//        }
        return mailService.sendToAuditor(manualVerifyDto) ? RUtil.success("提交成功，请耐心等待") :
                RUtil.error(ResultCode.MAIL_SEND_ERROR);
    }

    @ApiOperation("")
    @GetMapping("/manual/confirm/{token}")
    public void manualSetVerified(@PathVariable String token) {
        ManualVerifyDto manualVerifyDto = explainToken(token);
        userService.lambdaUpdate().eq(User::getId, Long.valueOf(token)).set(User::getStuId,
                manualVerifyDto.getStuId()).update();
        mailService.sendVerifiedEmail(manualVerifyDto);
    }

    @GetMapping("/manual/reject/{token}")
    public void manualRejectVerify(@PathVariable String token) {
        ManualVerifyDto manualVerifyDto = explainToken(token);
        mailService.sendUnVerifiedEmail(manualVerifyDto);
    }

    private ManualVerifyDto explainToken(String token) {
        String jsonStr = stringRedisTemplate.opsForValue().get(RedisConstant.MAIL_VERIFY_PREFIX + token);
        if (StrUtil.isBlank(jsonStr)) {
            throw new APIException(ResultCode.VERIFY_TOKEN_ERROR);
        }
        return JSONUtil.toBean(jsonStr, ManualVerifyDto.class);
    }

}
