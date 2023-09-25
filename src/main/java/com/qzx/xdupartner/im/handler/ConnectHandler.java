package com.qzx.xdupartner.im.handler;


import com.qzx.xdupartner.entity.ReqMessage;
import com.qzx.xdupartner.entity.RspMessage;
import com.qzx.xdupartner.im.OnlineUserHolder;
import com.qzx.xdupartner.util.JwtUtil;

import io.jsonwebtoken.Claims;
import io.netty.channel.ChannelHandlerContext;

public class ConnectHandler {
    public static void excute(ReqMessage reqMessage, ChannelHandlerContext ctx) {
        Long userId = null;
        try {
            Claims claims = JwtUtil.parseJWT(reqMessage.getToken());
            userId = Long.valueOf(claims.getSubject());
            if (!reqMessage.getFromId().equals(userId)) {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            ctx.channel().writeAndFlush(RspMessage.getSystemTextFrame("token有误"));
            OnlineUserHolder.removeChannel(ctx);
            return;
        }
        OnlineUserHolder.putUser(userId, ctx);
        ctx.writeAndFlush(RspMessage.getSystemTextFrame("连接im系统成功"));
    }
}
