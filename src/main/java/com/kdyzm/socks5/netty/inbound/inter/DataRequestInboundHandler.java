package com.kdyzm.socks5.netty.inbound.inter;

import com.kdyzm.socks5.netty.server.HubServer;
import com.kdyzm.socks5.netty.server.IServerContext;
import com.kdyzm.socks5.netty.server.InterClient;
import com.kdyzm.socks5.netty.server.MyClient;
import com.sun.corba.se.pept.transport.ByteBufferPool;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class DataRequestInboundHandler extends SimpleChannelInboundHandler {

    HubServer.HubMessage hubMessage = null;
    String channelId = null;
    IServerContext serverContext = null;

    public DataRequestInboundHandler(HubServer.HubMessage hubMessage, IServerContext serverContext) {
        this.hubMessage = hubMessage;
        this.serverContext = serverContext;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        channelId = UUID.randomUUID().toString();
        log.info("网页通道建立{}", ctx.channel());
        InterClient.sourceChannelConcurrentHashMap.put(channelId, ctx.channel());
        InterClient.sourceChannelConcurrentHashMapVerse.put(ctx.channel(), channelId);
        InterClient.interAndClientChannelID.put(channelId, hubMessage.sourceChannelId);
        InterClient.clientAndInterChannelID.put(hubMessage.sourceChannelId, channelId);
        ByteBuf byteBuf = Unpooled.copiedBuffer(hubMessage.data);
        ctx.channel().writeAndFlush(byteBuf);

        HubServer.HubMessage hubMessage_ = new HubServer.HubMessage();
        hubMessage_.type = HubServer.HubMessage.MsgType.pair;
        hubMessage_.sourceChannelId = channelId;
        hubMessage_.targetChannelId = hubMessage.sourceChannelId;
        hubMessage_.source = HubServer.HubMessage.MsgSource.local;
        serverContext.putMessage(hubMessage_);

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object o) throws Exception {

        ByteBuf result = (ByteBuf) o;
        byte[] data = new byte[result.readableBytes()];
        result.readBytes(data);

        HubServer.HubMessage hubMessage2 = hubMessage.clone();
        hubMessage2.data = data;
        hubMessage2.source = HubServer.HubMessage.MsgSource.local;
        hubMessage2.targetChannelId = hubMessage.sourceChannelId;
        hubMessage2.sourceChannelId = channelId;
        hubMessage2.type = HubServer.HubMessage.MsgType.tranformdata;
        serverContext.putMessage(hubMessage2);

        log.info("读取到读取网页信息{}", data.length);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        log.info("网页服务器Channel {} 关闭", channelId);
        if (null != channelId) {
            InterClient.sourceChannelConcurrentHashMap.remove(channelId);
            InterClient.sourceChannelConcurrentHashMapVerse.remove(ctx.channel());
        }
    }
}
