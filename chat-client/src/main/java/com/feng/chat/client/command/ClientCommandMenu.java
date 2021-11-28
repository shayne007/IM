package com.feng.chat.client.command;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.Scanner;

@Data
@Service("ClientCommandMenu")
public class ClientCommandMenu implements BaseCommand {

    public static final String KEY = "0";

    private String allCommandsShow;
    private String commandInput;

    @Override
    public boolean exec(Scanner scanner) {

        System.err.println("请输入某个操作指令：");
        System.err.println(allCommandsShow);
        //  获取第一个指令
        commandInput = scanner.next();
        return true;
    }


    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getTip() {
        return "show 所有命令";
    }

}
