package com.qzx.xdupartner.entity.vo;

import java.util.List;

import com.qzx.xdupartner.entity.RspMessage;

import lombok.Data;

@Data
public class MessageVo {

    private UserVo userVo;

    private List<RspMessage> messages;

    private Integer count;

    private Integer isFriend;
}
