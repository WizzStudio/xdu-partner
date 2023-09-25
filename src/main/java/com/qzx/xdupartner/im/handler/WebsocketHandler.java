package com.qzx.xdupartner.im.handler;


import com.qzx.xdupartner.constant.SystemConstant;
import com.qzx.xdupartner.entity.ReqMessage;
import com.qzx.xdupartner.entity.RspMessage;
import com.qzx.xdupartner.im.OnlineUserHolder;

import cn.hutool.json.JSONUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
//@Component
//@ChannelHandler.Sharable
public class WebsocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            log.info("心跳机制检测");
            OnlineUserHolder.removeChannel(ctx);
            ctx.close();
        }

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        ReqMessage reqMessage = null;
        try {
            reqMessage = JSONUtil.toBean(frame.text(), ReqMessage.class);
            if (reqMessage == null) {
                throw new RuntimeException("消息不能为空");
            }
        } catch (Exception e) {
            ctx.channel().writeAndFlush(RspMessage.getSystemTextFrame("消息类型转换错误"));
            log.error("消息类型转换错误" + e.getMessage());
            return;
        }
        switch (reqMessage.getCommand()) {
            case SystemConstant.CommandType.CONNECT:
                ConnectHandler.excute(reqMessage, ctx);
                break;
            case SystemConstant.CommandType.PING:
                PingHandler.excute(reqMessage, ctx);
                break;
            default:
                ctx.channel().writeAndFlush(RspMessage.getSystemTextFrame("不支持的消息类型"));
                break;
        }
    }
}
