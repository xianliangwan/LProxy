package com.kdyzm.socks5.netty;

import com.kdyzm.socks5.netty.config.Config;
import com.kdyzm.socks5.netty.server.HubServer;
import com.kdyzm.socks5.netty.server.InterClient;
import com.kdyzm.socks5.netty.server.MyClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author kdyzm
 * @date 2021-04-23
 */
@Slf4j
public class Main {

    public static void main(String[] args) throws InterruptedException {
        log.info("socks5 netty server is starting ......");

        try {

//            InputStr
//
//
//
//
//
//
//
//            new HubServer().start();
//            new InterClient().start();
            new MyClient().start();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
