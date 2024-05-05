package com.qzx.xdupartner.service;

import com.qzx.xdupartner.entity.dto.WxPhoneAuthDto;
import com.qzx.xdupartner.entity.vo.LoginVo;
import com.qzx.xdupartner.exception.APIException;

public interface WeChatService {
    LoginVo loginByPhoneCode(WxPhoneAuthDto wxPhoneAuthDto) throws APIException;

    LoginVo loginByOpenid(String loginCode) throws APIException;
}
