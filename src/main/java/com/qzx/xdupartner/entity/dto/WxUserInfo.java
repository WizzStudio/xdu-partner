package com.qzx.xdupartner.entity.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class WxUserInfo {
    private String appid;
    private String sessionKey;
    /**
     * 签名信息
     */
    private String signature;
    /**
     * 非敏感的用户信息
     */
    private String rawData;
    /**
     * 加密的数据
     */
    private String encryptedData;
    /**
     * 加密密钥
     */
    private String iv;
}
