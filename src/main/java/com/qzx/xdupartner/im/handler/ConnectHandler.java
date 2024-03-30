package com.qzx.xdupartner.im.handler;


import cn.hutool.json.JSONUtil;
import com.qzx.xdupartner.constant.RedisConstant;
import com.qzx.xdupartner.entity.ReqMessage;
import com.qzx.xdupartner.entity.RspMessage;
import com.qzx.xdupartner.entity.User;
import com.qzx.xdupartner.im.OnlineUserHolder;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ConnectHandler {
    @Resource
    StringRedisTemplate stringRedisTemplate;

    public void execute(ReqMessage reqMessage, ChannelHandlerContext ctx) {
        User user = JSONUtil.toBean(stringRedisTemplate.opsForValue().get(RedisConstant.LOGIN_PREFIX + reqMessage.getToken()), User.class);
        if (!reqMessage.getFromId().equals(user.getId())) {
            ctx.channel().writeAndFlush(RspMessage.getSystemTextFrame("token有误"));

            OnlineUserHolder.removeChannel(ctx);
            return;
        }
        OnlineUserHolder.putUser(user.getId(), ctx);
        ctx.writeAndFlush(RspMessage.getSystemTextFrame("连接im系统成功"));
    }
}
