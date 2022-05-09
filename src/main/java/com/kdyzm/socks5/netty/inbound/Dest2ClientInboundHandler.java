package com.kdyzm.socks5.netty.inbound;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kdyzm
 * @date 2021-04-24
 */
@Slf4j
public class Dest2ClientInboundHandler extends ChannelInboundHandlerAdapter {

    private final Channel channel;

    public Dest2ClientInboundHandler(Channel channel) {
        this.channel= channel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        log.trace("开始写回客户端");
        channel.writeAndFlush(msg);

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.trace("代理服务器和目标服务器的连接已经断开，即将断开客户端和代理服务器的连接");
        channel.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Dest2ClientInboundHandler exception", cause);
    }
}
