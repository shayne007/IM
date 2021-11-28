package com.feng.chat.client.command;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Scanner;

@Slf4j
@Data
@Service("LoginConsoleCommand")
public class LoginConsoleCommand implements BaseCommand {
    public static final String KEY = "1";

    private String userName;
    private String password;

    @Override
    public boolean exec(Scanner scanner) {

        System.out.println("请输入登录信息，格式为：用户名@密码 ");
        String s = scanner.next();
        String[] array = s.split("@");

        if (array.length != 2) {
            System.out.println("请输入登录信息，格式为：用户名@密码 ");
            return false;
        }
        userName = array[0];
        password = array[1];
        log.info("输入正确, 您输入的用户id是: {},密码是{}", userName, password);
        return true;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getTip() {
        return "登录";
    }

}
