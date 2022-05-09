package com.kdyzm.socks5.netty.inbound.myClient;

import com.kdyzm.socks5.netty.inbound.Client2DestInboundHandler;
import com.kdyzm.socks5.netty.inbound.Dest2ClientInboundHandler;
import com.kdyzm.socks5.netty.inbound.hub.HeartBeatInboundHandler;
import com.kdyzm.socks5.netty.server.HubServer;
import com.kdyzm.socks5.netty.server.IServerContext;
import com.kdyzm.socks5.netty.server.MyClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v5.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.MessageUtils;

import java.util.UUID;

public class DataExchangeInboundHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest> {

    private static final Logger logger = LoggerFactory.getLogger(HeartBeatInboundHandler.class);
    String id = null;
    EventLoopGroup nioEventLoopGroup = null;

    IServerContext serverContext = null;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        id = UUID.randomUUID().toString();
        MyClient.channelConcurrentHashMap.put(id,ctx.channel());
        MyClient.channelConcurrentHashMapVerse.put(ctx.channel(),id);
        logger.info("创建新的channel {}",id);
    }

    public DataExchangeInboundHandler(IServerContext serverContext,EventLoopGroup nioEventLoopGroup){
        this.nioEventLoopGroup = nioEventLoopGroup;
        this.serverContext = serverContext;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.info(cause.toString());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg) throws Exception {

        if (!msg.type().equals(Socks5CommandType.CONNECT)) {
            logger.debug("receive commandRequest type={}", msg.type());
            return;
        }


        logger.debug("准备连接目标服务器，ip={},port={},{},{}", msg.dstAddr(), msg.dstPort(),ctx.channel(),ctx.pipeline().names());



        ctx.pipeline().addLast(new Buffer2HubMessageInboundHandler(serverContext,msg.dstAddr(),msg.dstPort()));
        ctx.pipeline().remove(DataExchangeInboundHandler.class);
        ctx.pipeline().remove(Socks5CommandRequestDecoder.class);
        DefaultSocks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, msg.dstAddrType());
        ctx.writeAndFlush(commandResponse);




//        Bootstrap bootstrap = new Bootstrap();
//        bootstrap.group(nioEventLoopGroup)
//                .channel(NioSocketChannel.class)
//                .option(ChannelOption.TCP_NODELAY, true)
//                .handler(new ChannelInitializer<SocketChannel>() {
//                    @Override
//                    protected void initChannel(SocketChannel ch) throws Exception {
//                        //添加服务端写客户端的Handler
//                        ch.pipeline().addLast(new Dest2ClientInboundHandler(ctx.channel()));
//                    }
//                });
//        ChannelFuture future = bootstrap.connect(msg.dstAddr(), msg.dstPort());
//        future.addListener(new ChannelFutureListener() {
//            @Override
//            public void operationComplete(ChannelFuture future) throws Exception {
//                if (future.isSuccess()) {
//                    logger.debug("目标服务器连接成功");
//                    ctx.channel().pipeline().addLast(new Dest2ClientInboundHandler(future.channel()));
//                    DefaultSocks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, msg.dstAddrType());
//                    ctx.writeAndFlush(commandResponse);
//                    ctx.pipeline().remove(DataExchangeInboundHandler.class);
//                    ctx.pipeline().remove(Socks5CommandRequestDecoder.class);
//                } else {
//                    logger.error("连接目标服务器失败,address={},port={}", msg.dstAddr(), msg.dstPort());
//                    DefaultSocks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, msg.dstAddrType());
//                    ctx.writeAndFlush(commandResponse);
//                    future.channel().close();
//                }
//            }
//        });

    }
}
