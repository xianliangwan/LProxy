package com.kdyzm.socks5.netty.inbound;

import com.kdyzm.socks5.netty.server.HubServer;
import com.kdyzm.socks5.netty.server.IServerContext;
import com.kdyzm.socks5.netty.server.MyClient;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LineControllInboundHandler extends SimpleChannelInboundHandler {

    IServerContext serverContext = null;
    public LineControllInboundHandler(IServerContext serverContext){
        this.serverContext = serverContext;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {

        HubServer.HubMessage hubMessage = (HubServer.HubMessage) o;

        if(hubMessage.type == HubServer.HubMessage.MsgType.pair){

            MyClient.interAndClentChannelID.put(hubMessage.sourceChannelId,hubMessage.targetChannelId);
          //  log.info("Channel配对需求{} ,{}",hubMessage.sourceChannelId,hubMessage.targetChannelId);
            ReferenceCountUtil.release(o);
        }else if(hubMessage.type == HubServer.HubMessage.MsgType.tranformdata){

            if(MyClient.interAndClentChannelID.containsKey(hubMessage.sourceChannelId)){
                log.info("收到网页信息并写入{}",hubMessage.data.length);
                MyClient.channelConcurrentHashMap.get( MyClient.interAndClentChannelID.get(hubMessage.sourceChannelId))
                        .writeAndFlush(Unpooled.copiedBuffer(hubMessage.data));
            }
        }




    }
}
