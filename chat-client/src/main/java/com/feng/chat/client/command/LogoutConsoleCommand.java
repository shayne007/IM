package com.feng.chat.client.command;

import org.springframework.stereotype.Service;

import java.util.Scanner;

@Service("LogoutConsoleCommand")
public class LogoutConsoleCommand implements BaseCommand {
    public static final String KEY = "10";

    @Override
    public boolean exec(Scanner scanner) {
        return true;
    }


    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getTip() {
        return "退出";
    }

}
