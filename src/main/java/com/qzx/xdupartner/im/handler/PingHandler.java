package com.qzx.xdupartner.im.handler;


import com.qzx.xdupartner.entity.ReqMessage;
import com.qzx.xdupartner.entity.RspMessage;
import com.qzx.xdupartner.im.OnlineUserHolder;

import io.netty.channel.ChannelHandlerContext;

public class PingHandler {
    public static void excute(ReqMessage reqMessage, ChannelHandlerContext ctx) {
        if (OnlineUserHolder.containsChannel(ctx) && reqMessage.getContent() != null && reqMessage.getContent().equals("ping")) {
            ctx.writeAndFlush(RspMessage.getSystemTextFrame("pong"));
        } else {
            ctx.writeAndFlush(RspMessage.getSystemTextFrame("心跳检测格式不正确,channel关闭"));
            OnlineUserHolder.removeChannel(ctx);
        }
    }
}
