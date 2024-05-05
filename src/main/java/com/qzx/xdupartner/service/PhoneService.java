package com.qzx.xdupartner.service;

public interface PhoneService {
    boolean checkSent(String phone);

    boolean sendVerCode(String phone);
}
