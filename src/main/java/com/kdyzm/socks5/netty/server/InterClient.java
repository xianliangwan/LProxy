package com.kdyzm.socks5.netty.server;


import com.kdyzm.socks5.netty.config.Constant;
import com.kdyzm.socks5.netty.config.MarshallingCodeCFactory;
import com.kdyzm.socks5.netty.inbound.Client2DestInboundHandler;
import com.kdyzm.socks5.netty.inbound.Dest2ClientInboundHandler;
import com.kdyzm.socks5.netty.inbound.hub.HeartBeatInboundHandler;
import com.kdyzm.socks5.netty.inbound.hub.MsgDealHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.kdyzm.socks5.netty.server.HubServer.HubMessage.MsgType.lineConnect;

@Slf4j
public class InterClient extends IServer{

    private static final Logger logger = LoggerFactory.getLogger(InterClient.class);

    //本地的Channel
    public static Map<String,Channel> sourceChannelConcurrentHashMap = new ConcurrentHashMap<>();
    public static Map<Channel,String> sourceChannelConcurrentHashMapVerse = new ConcurrentHashMap<>();
    public static Map<String,String> interAndClientChannelID = new ConcurrentHashMap<>();
    public static Map<String,String> clientAndInterChannelID = new ConcurrentHashMap<>();


    public static EventLoopGroup eventExecutors = new NioEventLoopGroup();
    public static void main(String[] args) throws InterruptedException {


        InterClient interClient = new InterClient();

        HubServer.HubMessage lineConnectMsg = new HubServer.HubMessage();
        lineConnectMsg.type = lineConnect;
        interClient.putMessage(lineConnectMsg);

        interClient.sourceMark = HubServer.HubMessage.MsgSource.local;
    //    interClient.initLineControllChannel(new MsgDealHandler(interClient));
        interClient.initMessageWorker();
        try {
            while (true){

                Thread.sleep(1);
            }
        } catch (InterruptedException e) {
           logger.info(e.toString());
        }

    }


    

}
