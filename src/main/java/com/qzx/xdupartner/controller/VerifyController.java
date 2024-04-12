package com.qzx.xdupartner.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.qzx.xdupartner.constant.RedisConstant;
import com.qzx.xdupartner.entity.User;
import com.qzx.xdupartner.entity.vo.R;
import com.qzx.xdupartner.entity.vo.ResultCode;
import com.qzx.xdupartner.service.UserService;
import com.qzx.xdupartner.util.VerCodeGenerateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
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
    JavaMailSenderImpl mailSender;

    @ApiOperation("")
    @GetMapping("/sendCode")
    public R<String> sendCode(@RequestParam("stuId") String stuId) {
        String verCode = VerCodeGenerateUtil.getVerCode();
//一下为发送邮件部分
        MimeMessage mimeMessage = null;
        MimeMessageHelper helper = null;
        try {
            String time = DateUtil.format(new Date(), "yyyy-MM-dd hh:mm:ss");
            //发送复杂的邮件
            mimeMessage = mailSender.createMimeMessage();
            //组装
            try {
                helper = new MimeMessageHelper(mimeMessage, true);
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
            //邮件标题
            helper.setSubject("【仙电搭子】 注册账号验证码");
            //因为设置了邮件格式所以html标签有点多，后面的ture为支持识别html标签
            //想要不一样的邮件格式，百度搜索一个html编译器，自我定制。
            helper.setText("<h3>\n" +
                    "\t<span style=\"font-size:16px;\">亲爱的用户：</span> \n" +
                    "</h3>\n" +
                    "<p>\n" +
                    "\t<span style=\"font-size:14px;\">&nbsp;&nbsp;&nbsp;&nbsp;</span><span style=\"font-size:14px;" +
                    "\">&nbsp; <span style=\"font-size:16px;\">&nbsp;&nbsp;您正在进行邮箱验证，本次请求的验证码为：<span " +
                    "style=\"font-size:24px;color:#FFE500;\">" + verCode + "</span>,本验证码10分钟内有效，请在10" +
                    "分钟内完成验证。（请勿泄露此验证码）如非本人操作，请忽略该邮件。(这是一封自动发送的邮件，请不要直接回复）</span></span>\n" +
                    "</p>\n" +
                    "<p style=\"text-align:right;\">\n" +
                    "\t<span style=\"background-color:#FFFFFF;font-size:16px;color:#000000;\"><span " +
                    "style=\"color:#000000;font-size:16px;background-color:#FFFFFF;\"><span class=\"token string\" " +
                    "style=\"font-family:&quot;font-size:16px;color:#000000;line-height:normal !important;" +
                    "background-color:#FFFFFF;\">仙电搭子社区</span></span></span> \n" +
                    "</p>\n" +
                    "<p style=\"text-align:right;\">\n" +
                    "\t<span style=\"background-color:#FFFFFF;font-size:14px;\"><span style=\"color:#FF9900;" +
                    "font-size:18px;\"><span class=\"token string\" style=\"font-family:&quot;font-size:16px;" +
                    "color:#000000;line-height:normal !important;\"><span style=\"font-size:16px;color:#000000;" +
                    "background-color:#FFFFFF;\">" + time + "</span><span style=\"font-size:18px;color:#000000;" +
                    "background-color:#FFFFFF;\"></span></span></span></span> \n" +
                    "</p>", true);
            //收件人
            helper.setTo(stuId + "@stu.xidian.edu.cn");
            //发送方
            helper.setFrom("1500418656@qq.com");
            //发送邮件
            mailSender.send(mimeMessage);
        } catch (javax.mail.MessagingException e) {
//            发送失败--服务器繁忙
            return new R<>(ResultCode.MAIL_SEND_ERROR, "发送失败-服务器繁忙");
        } catch (MailException e) {
            //邮箱是无效的，或者发送失败
            return new R<>(ResultCode.MAIL_SEND_ERROR, "发送失败-邮箱无效");
        }
        //发送验证码成功
        stringRedisTemplate.opsForValue().set(RedisConstant.MAIL_CODE_PREFIX + stuId, verCode, 10, TimeUnit.MINUTES);
        return new R<>(ResultCode.SUCCESS, "发送成功");
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
            if (stuId.equals(sessionKey2)) {
                User user = userService.lambdaQuery().eq(User::getId, 14).one();
                return getStringR(sessionKey2, user);
            }
        }
        return new R<>(ResultCode.SUCCESS, "");
    }

    private R<String> getStringR(String sessionKey2, User user) {
        if (ObjectUtil.isNull(user)) {
            return null;
        }
        user.setSessionKey(sessionKey2);
        stringRedisTemplate.opsForValue().set(RedisConstant.LOGIN_PREFIX + sessionKey2,
                JSONUtil.toJsonStr(user),
                RedisConstant.LOGIN_VALID_TTL,
                TimeUnit.DAYS);
        return new R<>(ResultCode.SUCCESS, sessionKey2);
    }

}
