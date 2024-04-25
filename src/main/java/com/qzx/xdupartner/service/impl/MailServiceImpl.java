package com.qzx.xdupartner.service.impl;

import com.qzx.xdupartner.config.MailSenderConfig;
import com.qzx.xdupartner.service.MailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@AllArgsConstructor
public class MailServiceImpl implements MailService {

    private final MailSenderConfig senderConfig;

    @Override
    public boolean sendMail(String stuId, String verCode) {
//一下为发送邮件部分

        JavaMailSenderImpl mailSender = senderConfig.getSender();
        //创建SimpleMailMessage对象
        SimpleMailMessage message = new SimpleMailMessage();
        //邮件发送人
        message.setFrom(Objects.requireNonNull(mailSender.getUsername()));
        //邮件接收人
        message.setTo(stuId + "@stu.xidian.edu.cn");

        //邮件主题
        message.setSubject("【仙电搭子】注册账号验证码");
        //邮件内容
        message.setText("亲爱的用户：您正在进行邮箱验证，本次请求的验证码为：" + verCode + ",本验证码10分钟内有效，请在10分钟内完成验证。（请勿泄露此验证码）如非本人操作，请忽略该邮件。" +
                "(这是一封自动发送的邮件，请不要直接回复）");
        //发送邮件
        log.info("send message:" + message.toString());
        mailSender.send(message);

        return true;
    }
}