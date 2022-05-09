package com.kdyzm.socks5.netty.server;

import io.netty.channel.Channel;

public interface IServerContext {


    void putMessage(HubServer.HubMessage hubMessage);

    HubServer.HubMessage peekMessage();

    HubServer.HubMessage pollMessage();

    void sendHearBeat(HubServer.HubMessage.MsgSource source);

}
