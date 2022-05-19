package com.kdyzm.socks5.netty.inbound.hub;

import com.kdyzm.socks5.netty.inbound.Dest2ClientInboundHandler;
import com.kdyzm.socks5.netty.inbound.inter.DataRequestInboundHandler;
import com.kdyzm.socks5.netty.server.HubServer;
import com.kdyzm.socks5.netty.server.IServerContext;
import com.kdyzm.socks5.netty.server.InterClient;
import com.kdyzm.socks5.netty.server.MyClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class MsgDealHandler extends SimpleChannelInboundHandler {

    IServerContext serverContext = null;
    public MsgDealHandler(IServerContext serverContext){
        this.serverContext = serverContext;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object object) throws Exception {

        log.info("deal msg {}",object);
        HubServer.HubMessage hubMessage = (HubServer.HubMessage) object;
        if (hubMessage.source == HubServer.HubMessage.MsgSource.client) {
            if (hubMessage.type == HubServer.HubMessage.MsgType.request) {
                hubMessage.source = HubServer.HubMessage.MsgSource.server;
                if(null != HubServer.lineControlChannelLocal){
                    HubServer.lineControlChannelLocal.writeAndFlush(hubMessage);
                    log.info("发送信息给inter");
                }

                ReferenceCountUtil.release(object);
            }
        } else if (hubMessage.source == HubServer.HubMessage.MsgSource.server) {

            if (hubMessage.type == HubServer.HubMessage.MsgType.request) {
                log.info("收到request请求{}", hubMessage.sourceChannelId);


                if(null!=hubMessage.sourceChannelId&&InterClient.clientAndInterChannelID.containsKey(hubMessage.sourceChannelId)){


                    InterClient.sourceChannelConcurrentHashMap.get(InterClient.clientAndInterChannelID.get(hubMessage.sourceChannelId)).writeAndFlush(Unpooled.copiedBuffer(hubMessage.data));
                    log.info("写入数据到网页服务器已有得channel{}，字节数{}",
                            InterClient.sourceChannelConcurrentHashMap.get(InterClient.clientAndInterChannelID.get(hubMessage.sourceChannelId))
                            ,hubMessage.data.length);
                    return;
                }

                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(InterClient.eventExecutors)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                //读取

                            }
                        });
                ChannelFuture future = bootstrap.connect(hubMessage.url, hubMessage.port);
                future.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            log.debug("目标服务器连接成功from inter {}",serverContext);
                            //写
                            //ctx.channel().pipeline().addLast(new Dest2ClientInboundHandler(future.channel()));
                            future.channel().pipeline().addLast(new DataRequestInboundHandler(hubMessage,serverContext));
                            //future.channel().writeAndFlush(hubMessage.data);
                            //log.info("写入数据到网页服务器，字节数{}",hubMessage.data.length);

                        } else {
                            log.error("连接目标服务器失败,address={},port={}", hubMessage.url, hubMessage.port);
                            future.channel().close();
                        }
                    }
                });

            }else if (hubMessage.type == HubServer.HubMessage.MsgType.tranformdata) {

                MyClient.channelConcurrentHashMap.get(hubMessage.targetChannelId).writeAndFlush(hubMessage.data);
                ReferenceCountUtil.release(object);

            }

        } else if (hubMessage.source == HubServer.HubMessage.MsgSource.local) {

            if(hubMessage.type == HubServer.HubMessage.MsgType.pair){
                HubServer.lineControlChannelClient.writeAndFlush(hubMessage);
                return;
            }


            hubMessage.source = HubServer.HubMessage.MsgSource.server;
            hubMessage.type = HubServer.HubMessage.MsgType.tranformdata;
            if(null != HubServer.lineControlChannelClient){
                HubServer.lineControlChannelClient.writeAndFlush(hubMessage);
            }

            ReferenceCountUtil.release(object);

        }

    }
}
