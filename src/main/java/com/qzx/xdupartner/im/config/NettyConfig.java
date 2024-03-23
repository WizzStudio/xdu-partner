package com.qzx.xdupartner.im.config;


import com.qzx.xdupartner.im.handler.StatusHandler;
import com.qzx.xdupartner.im.handler.WebsocketHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.net.InetSocketAddress;

@Configuration
@Slf4j
public class NettyConfig {

    @Value("${netty.port}")
    private int port;

    @Resource
    private StatusHandler statusHandler;

    @Bean
    public EventLoopGroup bossGroup() {
        return new NioEventLoopGroup(1);
    }

    @Bean
    public EventLoopGroup workerGroup() {
        return new NioEventLoopGroup(10);
    }

    @Bean
    public ServerBootstrap serverBootstrap() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup(), workerGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()

                                .addLast(new HttpServerCodec())
                                //支持大数据流
                                .addLast(new ChunkedWriteHandler())
                                //对http消息做聚合
                                .addLast(new HttpObjectAggregator(1024 * 64))
                                //websocket
                                .addLast(new WebSocketServerProtocolHandler("/"))
                                .addLast(statusHandler)
                                .addLast(new IdleStateHandler(10, 10, 10))
                                .addLast(new WebsocketHandler());


                    }
                });
        log.info("IM-Server start up success!");
        return serverBootstrap;
    }

    @Bean
    public InetSocketAddress inetSocketAddress() {
        return new InetSocketAddress(port);
    }

    @Bean
    public ChannelFuture channelFuture() throws InterruptedException {
        return serverBootstrap().bind(inetSocketAddress()).sync();
    }
}
