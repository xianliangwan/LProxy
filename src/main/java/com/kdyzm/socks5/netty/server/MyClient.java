package com.kdyzm.socks5.netty.server;

import com.kdyzm.socks5.netty.config.Constant;
import com.kdyzm.socks5.netty.config.MarshallingCodeCFactory;
import com.kdyzm.socks5.netty.inbound.Dest2ClientInboundHandler;
import com.kdyzm.socks5.netty.inbound.LineControllInboundHandler;
import com.kdyzm.socks5.netty.inbound.Socks5InitialRequestInboundHandler;
import com.kdyzm.socks5.netty.inbound.Socks5PasswordAuthRequestInboundHandler;
import com.kdyzm.socks5.netty.inbound.hub.HeartBeatInboundHandler;
import com.kdyzm.socks5.netty.inbound.myClient.DataExchangeInboundHandler;
import com.kdyzm.socks5.netty.inbound.myClient.DataExchangeInboundHandlerFTP;
import com.kdyzm.socks5.netty.properties.ConfigProperties;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.kdyzm.socks5.netty.server.HubServer.HubMessage.MsgType.lineConnect;

@Slf4j
@AllArgsConstructor
public class MyClient extends IServer{

    public static Map<String,Channel> channelConcurrentHashMap = new ConcurrentHashMap<>();
    public static Map<Channel,String> channelConcurrentHashMapVerse = new ConcurrentHashMap<>();

    public static Map<String,String> interAndClentChannelID = new ConcurrentHashMap<>();


    public void start(){

        sourceMark = HubServer.HubMessage.MsgSource.client;

        HubServer.HubMessage lineConnectMsg = new HubServer.HubMessage();
        lineConnectMsg.type = lineConnect;
        putMessage(lineConnectMsg);
        initMessageWorker();

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        try {
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 256)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {

                            log.info("开始一个channel=========================");
                            ChannelPipeline pipeline = ch.pipeline();
                            //socks5响应最后一个encode
                            pipeline.addLast(Socks5ServerEncoder.DEFAULT);

                            //处理socks5初始化请求
                            pipeline.addLast(new Socks5InitialRequestDecoder());
                            pipeline.addLast(new Socks5InitialRequestInboundHandler());

//                            pipeline.addLast(new Socks5PasswordAuthRequestDecoder());
//                            pipeline.addLast(new Socks5PasswordAuthRequestInboundHandler());


                            //处理connection请求
                            pipeline.addLast(new Socks5CommandRequestDecoder());
                            pipeline.addLast(new DataExchangeInboundHandler(MyClient.this,commonWorkGroup));
                        //    pipeline.addLast(new DataExchangeInboundHandlerFTP(MyClient.this,commonWorkGroup));
                        }
                    });
            ChannelFuture future = bootstrap.bind(Constant.myClientPort);
            log.info("Socks5 Client has started on port {}", Constant.myClientPort);
            future.channel().closeFuture().sync();
        }catch (Exception e){
            log.info(e.toString());
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }




    }




//    public void start(){
//
//        MyClient myClient = new MyClient();
//        myClient.sourceMark = HubServer.HubMessage.MsgSource.client;
//     //   myClient.initLineControllChannel(new LineControllInboundHandler(myClient));
//
//        HubServer.HubMessage lineConnectMsg = new HubServer.HubMessage();
//        lineConnectMsg.type = lineConnect;
//        myClient.putMessage(lineConnectMsg);
//
//        myClient.initMessageWorker();
//        myClient.start();
//
//    }



}
