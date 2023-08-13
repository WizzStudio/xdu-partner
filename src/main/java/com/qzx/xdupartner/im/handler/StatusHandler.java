package com.qzx.xdupartner.im.handler;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import com.qzx.xdupartner.im.OnlineUserHolder;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ChannelHandler.Sharable
//在线人数
public class StatusHandler extends ChannelInboundHandlerAdapter {
    private static AtomicInteger count = new AtomicInteger(0);

    public static int getCount() {
        return count.intValue();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        int online = count.incrementAndGet();
        log.info("online:" + online);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        int online = count.decrementAndGet();
        log.info("online:" + online);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage());
        OnlineUserHolder.removeChannel(ctx);
    }
}
