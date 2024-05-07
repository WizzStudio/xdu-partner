package com.qzx.xdupartner.service;

import com.qzx.xdupartner.entity.dto.PhoneAuthDto;
import com.qzx.xdupartner.entity.vo.LoginVo;

public interface PhoneService {
    boolean checkSent(String phone);

    boolean sendVerCode(String phone);

    LoginVo verifyVerCode(PhoneAuthDto phoneAuthDto);
}
