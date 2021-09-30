package com.feng.chat.client;

import com.feng.chat.client.service.CommandManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/**
 * @Description chat client启动类
 * @Author fengsy
 * @Date 9/30/21
 */
@SpringBootApplication
public class ChatClientApplication {
    public static void main(String[] args) {
        // 启动并初始化 Spring 环境及其各 Spring 组件
        ApplicationContext context = SpringApplication.run(ChatClientApplication.class, args);
        CommandManager commandManager = context.getBean(CommandManager.class);

        commandManager.initCommandMap();
        try {
            commandManager.startCommandThread();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
