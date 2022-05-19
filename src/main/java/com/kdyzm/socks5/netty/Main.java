package com.kdyzm.socks5.netty;

import com.kdyzm.socks5.netty.config.Config;
import com.kdyzm.socks5.netty.config.Constant;
import com.kdyzm.socks5.netty.server.HubServer;
import com.kdyzm.socks5.netty.server.InterClient;
import com.kdyzm.socks5.netty.server.MyClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * @author kdyzm
 * @date 2021-04-23
 */
@Slf4j
public class Main {

    public static void main(String[] args) throws InterruptedException {
        log.info("socks5 netty server is starting ......");

        try {

            List<String> input = Arrays.asList(args);
            if(input.get(0).equals("1")){ //内网
                new InterClient().start(input.get(1),Integer.valueOf(input.get(2)));
            }else if(input.get(0).equals("2")){
                Constant.hubPort = Integer.parseInt(input.get(1));
                Constant.hubHost = "127.0.0.1";
                Constant.myClientPort = Integer.parseInt(input.get(2));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new HubServer().start();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                Thread.sleep(1000*3);
                new MyClient().start();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private static void out(String str){
        System.out.println(str);
    }
}
