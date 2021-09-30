package com.feng.chat.client;

import com.feng.chat.client.service.ChatServer;
import com.feng.chat.client.session.SessionManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @Description chat server 启动类
 * @Author fengsy
 * @Date 9/29/21
 */
@SpringBootApplication
public class ChatServerApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ChatServerApplication.class, args);

        /**
         * 将SessionManger 单例设置为spring bean
         */
        SessionManager sessionManger = context.getBean(SessionManager.class);
        SessionManager.setSingleInstance(sessionManger);

        /**
         * 启动服务
         */
        ChatServer nettyServer = context.getBean(ChatServer.class);
        nettyServer.run();
    }

}
