package com.qzx.xdupartner.service;

import com.qzx.xdupartner.entity.dto.ManualVerifyDto;

public interface MailService {

    /**
     * 发送邮件
     *
     * @return 返回 true 或者 false
     */

    boolean sendVerifyEmail(String stuId, String verCode);

    boolean sendToAuditor(ManualVerifyDto manualVerifyDto);
}
