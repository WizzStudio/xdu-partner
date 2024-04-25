package com.qzx.xdupartner.service;

public interface MailService {

    /**
     * 发送邮件
     * @return 返回 true 或者 false
     */

    boolean sendMail(String stuId, String verCode);
}
