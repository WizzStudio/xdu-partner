package com.qzx.xdupartner.util;


import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zlf
 * @description:
 * @time: 2022/8/31 11:10
 * 密钥必须为16字节或者16字节的倍数的字节型数据。
 * 明文必须为16字节或者16字节的倍数的字节型数据，如果不够16字节需要进行补全。
 */
@Slf4j
public class AesUtil {

    private static AES aes = null;

    /**
     * 16字节
     */
    private static String keyStr = "qzx";
    //长度为16位的盐值
    private static final String IV_KEY = "this_is_qzx_salt";

    static {
        // 构建
        //随机生成密钥
        SecretKeySpec secretKeySpec = new SecretKeySpec(getBytes(keyStr, 16),
                SymmetricAlgorithm.AES.getValue());
        IvParameterSpec ivParameterSpec = new IvParameterSpec(IV_KEY.getBytes());
        aes = new AES(Mode.CBC, Padding.PKCS5Padding, secretKeySpec, ivParameterSpec);
    }

    private static byte[] getBytes(String s, int length) {
        int fixLength = length - s.getBytes().length;
        if (s.getBytes().length < length) {
            byte[] S_bytes = new byte[length];
            System.arraycopy(s.getBytes(), 0, S_bytes, 0, s.getBytes().length);
            for (int x = length - fixLength; x < length; x++) {
                S_bytes[x] = 0x00;
            }
            return S_bytes;
        }
        return s.getBytes();
    }

    /**
     * 加密
     *
     * @param content
     * @return
     */
    public static String encryptHex(String content) {
        // 加密
        byte[] encrypt = aes.encrypt(content);
        // 加密为16进制表示
        String encryptHex = aes.encryptHex(content);
        return encryptHex;
    }

    /**
     * 解密
     *
     * @param encryptHex
     * @return
     */
    public static String decryptHex(String encryptHex) {
        // 解密为字符串
        String decryptStr = aes.decryptStr(encryptHex, CharsetUtil.CHARSET_UTF_8);
        return decryptStr;
    }

    public static void main(String[] args) {
        String content = "test中文";
        log.info("content:{}", content);
        // 加密为16进制表示
        String encryptHex = encryptHex(content);
        log.info("encryptHex:{}", encryptHex.toUpperCase());
        // 解密为字符串
        String decryptStr = decryptHex(encryptHex);
        log.info("decryptStr:{}", decryptStr);
    }

}
