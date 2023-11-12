package com.qzx.xdupartner.im;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.qzx.xdupartner.entity.RspMessage;
import com.qzx.xdupartner.util.UserHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class OnlineUserHolder {
    private static final Map<Long, ChannelHandlerContext> USER_ONLINE = new ConcurrentHashMap<>();

    public static void putUser(Long id, ChannelHandlerContext ctx) {
        USER_ONLINE.put(id, ctx);
    }

    public static void removeUser(Long id) {
        USER_ONLINE.remove(id);
    }

    public static void removeChannel(ChannelHandlerContext ctx) {
        ctx.close();
        USER_ONLINE.entrySet().stream().filter(entry -> entry.getValue() == ctx).forEach(entry -> USER_ONLINE.remove(entry.getKey()));
    }

    public static boolean containsChannel(ChannelHandlerContext ctx) {
        return USER_ONLINE.containsValue(ctx);
    }

    public static ChannelHandlerContext getUserChannel(Long userId) {
        return USER_ONLINE.get(userId);
    }

    public static boolean sendText(Long toId, RspMessage rspMessage) {
        ChannelHandlerContext channelHandlerContext = USER_ONLINE.get(toId);
        if (ObjectUtil.isNull(channelHandlerContext)) {
            return false;
        }
        rspMessage.setFromId(UserHolder.getUserId());
        String text = JSONUtil.toJsonStr(rspMessage);
        channelHandlerContext.writeAndFlush(new TextWebSocketFrame(text));
        log.info(text);
        return true;
    }
}
