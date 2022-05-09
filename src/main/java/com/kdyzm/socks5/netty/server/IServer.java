package com.kdyzm.socks5.netty.server;

import com.kdyzm.socks5.netty.config.Constant;
import com.kdyzm.socks5.netty.config.MarshallingCodeCFactory;
import com.kdyzm.socks5.netty.inbound.LineControllInboundHandler;
import com.kdyzm.socks5.netty.inbound.hub.MsgDealHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
public class IServer implements IServerContext {

    public Queue<HubServer.HubMessage> messageQueue = new ConcurrentLinkedQueue();

    public static Channel lineControlChannelClient = null;
    public static Channel lineControlChannelLocal = null;

    public HubServer.HubMessage.MsgSource sourceMark;

    public EventLoopGroup commonWorkGroup = new NioEventLoopGroup();


    public void initMessageWorker() {

        Thread messageWorker = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    HubServer.HubMessage hubMessage = null;
                    long time = System.currentTimeMillis();
                    while (true) {

                        if (null != (hubMessage = peekMessage())) {
                            hubMessage = pollMessage();
                            if (hubMessage.type == HubServer.HubMessage.MsgType.lineConnect) {
                                log.info("lineControll 重新链接");
                                if (sourceMark == HubServer.HubMessage.MsgSource.local) {
                                    initLineControllChannel(new MsgDealHandler(IServer.this));
                                } else if (sourceMark == HubServer.HubMessage.MsgSource.client) {
                                    initLineControllChannel(new LineControllInboundHandler(IServer.this));
                                }
                            } else if (null != lineControlChannelLocal && lineControlChannelLocal.isActive()) {
                                lineControlChannelLocal.writeAndFlush(hubMessage);
                                log.info("hubMsg发送成功");
                            } else if (null != lineControlChannelLocal && !lineControlChannelLocal.isActive()) {
                                HubServer.HubMessage lineConnectMsg = new HubServer.HubMessage();
                                lineConnectMsg.type = HubServer.HubMessage.MsgType.lineConnect;
                                putMessage(lineConnectMsg);
                            }
                        } else if ((System.currentTimeMillis() - time) > 1000 * 10) {

                            if (null == lineControlChannelLocal || !lineControlChannelLocal.isActive()) {

                                HubServer.HubMessage lineConnectMsg = new HubServer.HubMessage();
                                lineConnectMsg.type = HubServer.HubMessage.MsgType.lineConnect;
                                putMessage(lineConnectMsg);

                            } else {
                                sendHearBeat(sourceMark);
                            }

                            time = System.currentTimeMillis();
                            Thread.sleep(1);
                            log.info("休息一下");
                        }

                    }
                } catch (Exception e) {
                    log.error(e.toString());
                }
            }
        });

        messageWorker.setDaemon(true);
        messageWorker.start();
    }

    @Override
    public void putMessage(HubServer.HubMessage hubMessage) {
        messageQueue.offer(hubMessage);
    }

    @Override
    public HubServer.HubMessage peekMessage() {
        return messageQueue.peek();
    }

    @Override
    public HubServer.HubMessage pollMessage() {
        return messageQueue.poll();
    }

    public void initLineControllChannel(ChannelInboundHandler... channelInboundHandlers) {

        Thread lineThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Bootstrap bootstrap = new Bootstrap();
                    bootstrap.group(commonWorkGroup)
                            .channel(NioSocketChannel.class)
                            .option(ChannelOption.TCP_NODELAY, true)
                            .handler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch) throws Exception {
                                    //添加服务端写客户端的Handler
                                    ch.pipeline().addLast(MarshallingCodeCFactory.buliteMarshallingDecoder());
                                    ch.pipeline().addLast(MarshallingCodeCFactory.buliteMarshallingEncoder());
                                    //  ch.pipeline().addLast(new LineControllInboundHandler(IServer.this));
                                    if (null != channelInboundHandlers && channelInboundHandlers.length > 0) {
                                        ch.pipeline().addLast(channelInboundHandlers);
                                    }


                                }
                            });
                    log.info("链接服务器{}  {}", Constant.hubHost, Constant.hubPort);
                    ChannelFuture future = bootstrap.connect(Constant.hubHost, Constant.hubPort);
                    future.addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (future.isSuccess()) {
                                log.debug("Hub服务器连接成功");
                                lineControlChannelLocal = future.channel();

                                sendHearBeat(sourceMark);

                            } else {
                                log.error("连接Hub服务器失败{}", future.cause().toString());
                                future.channel().close();
                            }
                        }
                    });

                    future.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    log.info(e.toString());
                }

            }
        });

        lineThread.setDaemon(true);
        lineThread.start();

    }

    @Override
    public void sendHearBeat(HubServer.HubMessage.MsgSource source) {

        HubServer.HubMessage hubMessage = new HubServer.HubMessage();
        hubMessage.type = HubServer.HubMessage.MsgType.heartbeat;
        hubMessage.source = source;
        putMessage(hubMessage);

    }

}
