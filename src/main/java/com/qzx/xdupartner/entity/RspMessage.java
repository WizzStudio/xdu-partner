package com.qzx.xdupartner.entity;

import cn.hutool.json.JSONUtil;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class RspMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long fromId;//来自谁的消息 1 为系统消息

    private Long id;//消息id

    private String content;

    private int type;

    private LocalDateTime createTime;

    public static TextWebSocketFrame getSystemTextFrame(String content) {
        RspMessage message = new RspMessage();
        message.setFromId(0L);
        message.setContent(content);
        message.setType(1);
        return new TextWebSocketFrame(JSONUtil.toJsonStr(message));
    }

}
