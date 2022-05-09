package com.kdyzm.socks5.netty.inbound.hub;

import com.kdyzm.socks5.netty.server.HubServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeartBeatInboundHandler extends SimpleChannelInboundHandler {


    private static final Logger logger = LoggerFactory.getLogger(HeartBeatInboundHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object object) throws Exception {

        HubServer.HubMessage hubMessage = (HubServer.HubMessage) object;

        if (hubMessage.type == HubServer.HubMessage.MsgType.heartbeat) {

            if (hubMessage.source == HubServer.HubMessage.MsgSource.local) {

                logger.info("收到inter的心跳包");

                HubServer.lineControlChannelLocal = ctx.channel();

            }
             else if (hubMessage.source == HubServer.HubMessage.MsgSource.client) {
                    logger.info("收到client的心跳包");

                    HubServer.lineControlChannelClient = ctx.channel();

                }


            } else {

                ctx.fireChannelRead(object);
            }


        }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        logger.info("有新的客户端连接");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.info("断开连接");
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.info(cause.toString());
    }
}
