package com.qzx.xdupartner.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.qzx.xdupartner.config.MailSenderConfig;
import com.qzx.xdupartner.constant.RedisConstant;
import com.qzx.xdupartner.entity.dto.ManualVerifyDto;
import com.qzx.xdupartner.entity.vo.ResultCode;
import com.qzx.xdupartner.exception.APIException;
import com.qzx.xdupartner.service.MailService;
import com.qzx.xdupartner.util.UserHolder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@AllArgsConstructor
public class MailServiceImpl implements MailService {
    //    public static final String endpoint = "https://xdu-partner.be.wizzstudio.com/wz/";
    public static final String endpoint = "http://localhost:8080/wz/";
    private static final String[] adminEmailList = new String[]{"1500418656@qq.com"};
    private final MailSenderConfig senderConfig;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean sendVerifyEmail(String stuId, String verCode) {
        JavaMailSenderImpl mailSender = senderConfig.getSender();
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
        try {
            //创建SimpleMailMessage对象
            //邮件发送人
            messageHelper.setFrom(Objects.requireNonNull(mailSender.getUsername()));
            //邮件接收人
            String toEmail = stuId + "@stu.xidian.edu.cn";
            messageHelper.setTo(toEmail);
            //邮件主题
            messageHelper.setSubject("【仙电搭子】校园邮箱认证验证码");
            //邮件内容
            messageHelper.setText(StrUtil.replace(emailVerCodeTemplate, "{ver_code}", verCode), true);
            //发送邮件
            log.info("send verified message to [{}], from [{}], ver_code:[{}]", toEmail, mailSender.getUsername(),
                    verCode);
            mailSender.send(messageHelper.getMimeMessage());
        } catch (MessagingException e) {
            throw new APIException(ResultCode.MAIL_SEND_ERROR);
        }

        //发送验证码成功
        stringRedisTemplate.opsForValue().set(RedisConstant.MAIL_CODE_PREFIX + stuId, verCode, 10,
                TimeUnit.MINUTES);
        return true;
    }

    @Override
    public boolean sendToAuditor(ManualVerifyDto manualVerifyDto) {
        for (String email : adminEmailList) {
            String token = generateToken(manualVerifyDto);
            String text = StrUtil.replace(manualVerifyTemplate, "{reject_url}", endpoint + "verify/manual/reject" +
                    "/" + token);
            text = StrUtil.replace(text, "{confirm_url}", endpoint + "verify/manual/confirm/" + token);
            text = StrUtil.replace(text, "{online_url}",
                    "https://www.chsi.com.cn/xlcx/bg.do?vcode=" + manualVerifyDto.getOnlineVerCode());
            sendEmail(email, text);
        }

        return true;
    }

    @Override
    public void sendVerifiedEmail(ManualVerifyDto manualVerifyDto) {
        sendEmail(manualVerifyDto.getLeftEmail(), verifiedTemplate);
    }

    @Override
    public void sendUnVerifiedEmail(ManualVerifyDto manualVerifyDto) {
        sendEmail(manualVerifyDto.getLeftEmail(), unVerifiedTemplate);
    }

    private void sendEmail(String email, String text) {
        try {

            JavaMailSenderImpl mailSender = senderConfig.getSender();
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom(Objects.requireNonNull(mailSender.getUsername()));
            messageHelper.setTo(email);
            messageHelper.setSubject("【仙电搭子】人工验证");
            messageHelper.setText(text, true);
            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new APIException(ResultCode.MAIL_SEND_ERROR);
        }
    }

    private String generateToken(ManualVerifyDto manualVerifyDto) {
        stringRedisTemplate.opsForValue().set(RedisConstant.MAIL_VERIFY_PREFIX + UserHolder.getUserId(),
                JSONUtil.toJsonStr(manualVerifyDto), 3, TimeUnit.DAYS);
        return UserHolder.getUserId().toString();
    }

    public static final String manualVerifyTemplate = "{online_url}   {confirm_url}   {reject_url}";
    public static final String emailVerCodeTemplate = "<!DOCTYPE html>\n" +
            "<html xmlns:th=\"http://www.thymeleaf.org\">\n" +
            "<head>\n" +
            "    <meta charset=\"utf-8\">\n" +
            "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
            "    <meta name=\"description\" content=\"email code\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
            "</head>\n" +
            "<!--邮箱验证码模板-->\n" +
            "<body>\n" +
            "<div style=\"background-color:#ECECEC; padding: 35px;\">\n" +
            "    <table cellpadding=\"0\" align=\"center\"\n" +
            "           style=\"width: 800px;height: 100%; margin: 0px auto; text-align: left; position: relative; " +
            "border-top-left-radius: 5px; border-top-right-radius: 5px; border-bottom-right-radius: 5px; " +
            "border-bottom-left-radius: 5px; font-size: 14px; font-family:微软雅黑, 黑体; line-height: 1.5; box-shadow: rgb" +
            "(153, 153, 153) 0px 0px 5px; border-collapse: collapse; background-position: initial initial; " +
            "background-repeat: initial initial;background:#fff;\">\n" +
            "        <tbody>\n" +
            "        <tr>\n" +
            "            <th valign=\"middle\"\n" +
            "                style=\"height: 25px; line-height: 25px; padding: 15px 35px; border-bottom-width: 1px; " +
            "border-bottom-style: solid; border-bottom-color: RGB(148,0,211); background-color: RGB(148,0,211); " +
            "border-top-left-radius: 5px; border-top-right-radius: 5px; border-bottom-right-radius: 0px; " +
            "border-bottom-left-radius: 0px;\">\n" +
            "                <font face=\"微软雅黑\" size=\"5\" style=\"color: rgb(255, 255, 255); \">仙电搭子校园邮箱认证</font>\n" +
            "            </th>\n" +
            "        </tr>\n" +
            "        <tr>\n" +
            "            <td style=\"word-break:break-all\">\n" +
            "                <div style=\"padding:25px 35px 40px; background-color:#fff;opacity:0.8;\">\n" +
            "\n" +
            "                    <h2 style=\"margin: 5px 0px; \">\n" +
            "                        <font color=\"#333333\" style=\"line-height: 20px; \">\n" +
            "                            <font style=\"line-height: 22px; \" size=\"4\">\n" +
            "                                尊敬的用户：</font>\n" +
            "                        </font>\n" +
            "                    </h2>\n" +
            "                    <!-- 中文 -->\n" +
            "                    <p>您好！感谢您使用仙电搭子，您的账号正在进行邮箱验证，验证码为：<font " +
            "color=\"#ff8c00\">{ver_code}</font>，有效期10分钟，请尽快填写验证码完成验证！</p><br>\n" +
            "                    <!-- 英文 -->\n" +
            "                    <h2 style=\"margin: 5px 0px; \">\n" +
            "                        <font color=\"#333333\" style=\"line-height: 20px; \">\n" +
            "                            <font style=\"line-height: 22px; \" size=\"4\">\n" +
            "                                Dear user:</font>\n" +
            "                        </font>\n" +
            "                    </h2>\n" +
            "                    <div style=\"width:100%;margin:0 auto;\">\n" +
            "                        <div style=\"padding:10px 10px 0;border-top:1px solid #ccc;color:#747474;" +
            "margin-bottom:20px;line-height:1.3em;font-size:12px;\">\n" +
            "                            <p>仙电搭子团队</p>\n" +
            "                            <p>联系我们：qq群：195628637</p>\n" +
            "                            <br>\n" +
            "                            <p>此为系统邮件，请勿回复<br>\n" +
            "                            </p>\n" +
            "                        </div>\n" +
            "                    </div>\n" +
            "                </div>\n" +
            "            </td>\n" +
            "        </tr>\n" +
            "        </tbody>\n" +
            "    </table>\n" +
            "</div>\n" +
            "</body>\n" +
            "</html>\n";

    public static final String verifiedTemplate = "<!DOCTYPE html>\n" +
            "<html xmlns:th=\"http://www.thymeleaf.org\">\n" +
            "<head>\n" +
            "    <meta charset=\"utf-8\">\n" +
            "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
            "    <meta name=\"description\" content=\"email code\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
            "</head>\n" +
            "<body>\n" +
            "<div style=\"background-color:#ECECEC; padding: 35px;\">\n" +
            "    <table cellpadding=\"0\" align=\"center\"\n" +
            "           style=\"width: 800px;height: 100%; margin: 0px auto; text-align: left; position: relative; " +
            "border-top-left-radius: 5px; border-top-right-radius: 5px; border-bottom-right-radius: 5px; " +
            "border-bottom-left-radius: 5px; font-size: 14px; font-family:微软雅黑, 黑体; line-height: 1.5; box-shadow: rgb" +
            "(153, 153, 153) 0px 0px 5px; border-collapse: collapse; background-position: initial initial; " +
            "background-repeat: initial initial;background:#fff;\">\n" +
            "        <tbody>\n" +
            "        <tr>\n" +
            "            <th valign=\"middle\"\n" +
            "                style=\"height: 25px; line-height: 25px; padding: 15px 35px; border-bottom-width: 1px; " +
            "border-bottom-style: solid; border-bottom-color: RGB(148,0,211); background-color: RGB(148,0,211); " +
            "border-top-left-radius: 5px; border-top-right-radius: 5px; border-bottom-right-radius: 0px; " +
            "border-bottom-left-radius: 0px;\">\n" +
            "                <font face=\"微软雅黑\" size=\"5\" style=\"color: rgb(255, 255, 255); \">仙电搭子校园认证成功</font>\n" +
            "            </th>\n" +
            "        </tr>\n" +
            "        <tr>\n" +
            "            <td style=\"word-break:break-all\">\n" +
            "                <div style=\"padding:25px 35px 40px; background-color:#fff;opacity:0.8;\">\n" +
            "\n" +
            "                    <h2 style=\"margin: 5px 0px; \">\n" +
            "                        <font color=\"#333333\" style=\"line-height: 20px; \">\n" +
            "                            <font style=\"line-height: 22px; \" size=\"4\">\n" +
            "                                尊敬的用户：</font>\n" +
            "                        </font>\n" +
            "                    </h2>\n" +
            "                    <!-- 中文 -->\n" +
            "                    <p>您好！感谢您使用仙电搭子，您的账号已认证成功！可以在社区内进行发帖、聊天等操作！</p><br>\n" +
            "                    <!-- 英文 -->\n" +
            "                    <h2 style=\"margin: 5px 0px; \">\n" +
            "                        <font color=\"#333333\" style=\"line-height: 20px; \">\n" +
            "                            <font style=\"line-height: 22px; \" size=\"4\">\n" +
            "                                Dear user:</font>\n" +
            "                        </font>\n" +
            "                    </h2>\n" +
            "                    <div style=\"width:100%;margin:0 auto;\">\n" +
            "                        <div style=\"padding:10px 10px 0;border-top:1px solid #ccc;color:#747474;" +
            "margin-bottom:20px;line-height:1.3em;font-size:12px;\">\n" +
            "                            <p>仙电搭子团队</p>\n" +
            "                            <p>联系我们：qq群：195628637</p>\n" +
            "                            <br>\n" +
            "                            <p>此为系统邮件，请勿回复<br>\n" +
            "                            </p>\n" +
            "                        </div>\n" +
            "                    </div>\n" +
            "                </div>\n" +
            "            </td>\n" +
            "        </tr>\n" +
            "        </tbody>\n" +
            "    </table>\n" +
            "</div>\n" +
            "</body>\n" +
            "</html>\n";

    public static final String unVerifiedTemplate = "<!DOCTYPE html>\n" +
            "<html xmlns:th=\"http://www.thymeleaf.org\">\n" +
            "<head>\n" +
            "    <meta charset=\"utf-8\">\n" +
            "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
            "    <meta name=\"description\" content=\"email code\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
            "</head>\n" +
            "<body>\n" +
            "<div style=\"background-color:#ECECEC; padding: 35px;\">\n" +
            "    <table cellpadding=\"0\" align=\"center\"\n" +
            "           style=\"width: 800px;height: 100%; margin: 0px auto; text-align: left; position: relative; " +
            "border-top-left-radius: 5px; border-top-right-radius: 5px; border-bottom-right-radius: 5px; " +
            "border-bottom-left-radius: 5px; font-size: 14px; font-family:微软雅黑, 黑体; line-height: 1.5; box-shadow: rgb" +
            "(153, 153, 153) 0px 0px 5px; border-collapse: collapse; background-position: initial initial; " +
            "background-repeat: initial initial;background:#fff;\">\n" +
            "        <tbody>\n" +
            "        <tr>\n" +
            "            <th valign=\"middle\"\n" +
            "                style=\"height: 25px; line-height: 25px; padding: 15px 35px; border-bottom-width: 1px; " +
            "border-bottom-style: solid; border-bottom-color: RGB(148,0,211); background-color: RGB(148,0,211); " +
            "border-top-left-radius: 5px; border-top-right-radius: 5px; border-bottom-right-radius: 0px; " +
            "border-bottom-left-radius: 0px;\">\n" +
            "                <font face=\"微软雅黑\" size=\"5\" style=\"color: rgb(255, 255, 255); \">仙电搭子校园认证失败</font>\n" +
            "            </th>\n" +
            "        </tr>\n" +
            "        <tr>\n" +
            "            <td style=\"word-break:break-all\">\n" +
            "                <div style=\"padding:25px 35px 40px; background-color:#fff;opacity:0.8;\">\n" +
            "\n" +
            "                    <h2 style=\"margin: 5px 0px; \">\n" +
            "                        <font color=\"#333333\" style=\"line-height: 20px; \">\n" +
            "                            <font style=\"line-height: 22px; \" size=\"4\">\n" +
            "                                尊敬的用户：</font>\n" +
            "                        </font>\n" +
            "                    </h2>\n" +
            "                    <!-- 中文 -->\n" +
            "                    <p>您好！感谢您使用仙电搭子，很遗憾，您的账号认证失败！请检查信息和学信网在线验证码后重新发起认证。</p><br>\n" +
            "                    <!-- 英文 -->\n" +
            "                    <h2 style=\"margin: 5px 0px; \">\n" +
            "                        <font color=\"#333333\" style=\"line-height: 20px; \">\n" +
            "                            <font style=\"line-height: 22px; \" size=\"4\">\n" +
            "                                Dear user:</font>\n" +
            "                        </font>\n" +
            "                    </h2>\n" +
            "                    <div style=\"width:100%;margin:0 auto;\">\n" +
            "                        <div style=\"padding:10px 10px 0;border-top:1px solid #ccc;color:#747474;" +
            "margin-bottom:20px;line-height:1.3em;font-size:12px;\">\n" +
            "                            <p>仙电搭子团队</p>\n" +
            "                            <p>联系我们：qq群：195628637</p>\n" +
            "                            <br>\n" +
            "                            <p>此为系统邮件，请勿回复<br>\n" +
            "                            </p>\n" +
            "                        </div>\n" +
            "                    </div>\n" +
            "                </div>\n" +
            "            </td>\n" +
            "        </tr>\n" +
            "        </tbody>\n" +
            "    </table>\n" +
            "</div>\n" +
            "</body>\n" +
            "</html>\n";

}