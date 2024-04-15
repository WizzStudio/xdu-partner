package com.qzx.xdupartner.entity;

import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Result {
    WxMaJscode2SessionResult result;
    Long userId;
}
