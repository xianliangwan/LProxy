package com.kdyzm.socks5.netty.server;

import com.kdyzm.socks5.netty.config.Constant;
import com.kdyzm.socks5.netty.config.MarshallingCodeCFactory;
import com.kdyzm.socks5.netty.inbound.hub.HeartBeatInboundHandler;
import com.kdyzm.socks5.netty.inbound.hub.MsgDealHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class HubServer extends IServer{


    public static List<Channel> freeChannel = new ArrayList<>();
    public static List<Channel> usedChannel = new ArrayList<>();

    public static class HubMessage implements Serializable {

        public enum MsgType{
            heartbeat ,write ,read ,newfreechannel,request,tranformdata,pair,lineConnect
        }

        public enum MsgSource{
            local ,server,client
        }

        public MsgType type;
        public MsgSource source ;//local ,server,client
        public int free = 0;//hub的空闲通道数量

        public String url;
        public int port;

        public byte[] data;

        public String sourceChannelId,targetChannelId;



        public HubMessage clone() throws CloneNotSupportedException {

            HubMessage hubMessage = new HubMessage();
            hubMessage.type = type;//heartbeat ,write ,read ,newfreechannel,request,tranformdata
            hubMessage.source = source;//local ,server,client
            hubMessage.free = free;//hub的空闲通道数量
            hubMessage.url = url;
            hubMessage.port = port;
            hubMessage.data = data;
            hubMessage.sourceChannelId = sourceChannelId;
            hubMessage.targetChannelId = targetChannelId;
            return hubMessage;
        }


    }


    public static void main(String[] args) throws InterruptedException {


        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(4);
        ServerBootstrap bootstrap = new ServerBootstrap();
        try {
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 256)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(MarshallingCodeCFactory.buliteMarshallingDecoder());
                            pipeline.addLast(new HeartBeatInboundHandler());
                            pipeline.addLast(new MsgDealHandler(null));


                            pipeline.addLast(MarshallingCodeCFactory.buliteMarshallingEncoder());

                        }
                    });
            ChannelFuture future = bootstrap.bind(Constant.hubPort).sync();
            log.info("Hub Server has started on port {}", Constant.hubPort);
            future.channel().closeFuture().sync();
        }  finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }





    }

}
