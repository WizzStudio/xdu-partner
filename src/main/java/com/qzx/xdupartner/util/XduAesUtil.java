package com.qzx.xdupartner.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class XduAesUtil {
    private final static String encryptType = "AES/CBC/PKCS5Padding";
    private final static String encryptTypeName = "AES";
    private final static String ivKey = "xidianscriptsxdu";
    private final static String ivKeyX4 = "xidianscriptsxduxidianscriptsxduxidianscriptsxduxidianscriptsxdu";

    static String encrypt(String value, String enc) throws Exception {
        Cipher crypt = Cipher.getInstance(encryptType);
        SecretKeySpec key = new SecretKeySpec(enc.getBytes(), encryptTypeName);
        IvParameterSpec iv = new IvParameterSpec(ivKey.getBytes());
        crypt.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] encrypted =
                crypt.doFinal((ivKeyX4 + value).getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }
}
