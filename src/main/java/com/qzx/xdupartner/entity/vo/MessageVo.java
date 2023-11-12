package com.qzx.xdupartner.entity.vo;

import com.qzx.xdupartner.entity.RspMessage;
import lombok.Data;

import java.util.List;

@Data
public class MessageVo {

    private UserVo userVo;

    private List<RspMessage> messages;

    private Integer count;

    private Integer isFriend;
}
