package com.kdyzm.socks5.netty.inbound.myClient;

import com.kdyzm.socks5.netty.server.HubServer;
import com.kdyzm.socks5.netty.server.IServerContext;
import com.kdyzm.socks5.netty.server.MyClient;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandRequest;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Buffer2HubMessageInboundHandler extends SimpleChannelInboundHandler {

    String url ;
    int port;
    IServerContext serverContext = null;
    public Buffer2HubMessageInboundHandler(IServerContext serverContext, String url ,int port){
        this.url = url;
        this.port = port;
        this.serverContext = serverContext;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        log.info("socket5 Channel 激活{}",ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object o) throws Exception {

        ByteBuf result = (ByteBuf) o;
        byte[] data = new byte[result.readableBytes()];
        result.readBytes(data);

        HubServer.HubMessage hubMessage = new HubServer.HubMessage();
        hubMessage.url = url;
        hubMessage.port = port;
        hubMessage.type = HubServer.HubMessage.MsgType.request;
        hubMessage.sourceChannelId = MyClient.channelConcurrentHashMapVerse.get(ctx.channel());
        hubMessage.data = data;
        hubMessage.source = HubServer.HubMessage.MsgSource.client;

        if(null != serverContext){

            serverContext.putMessage(hubMessage);
            log.info("发送请求信息 from buffer2Hub {}",ctx.channel());
         //   ReferenceCountUtil.release(o);
        }


    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);

        log.info(cause.toString());
    }
}
