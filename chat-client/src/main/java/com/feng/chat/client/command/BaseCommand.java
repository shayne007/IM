package com.feng.chat.client.command;

import java.util.Scanner;

public interface BaseCommand {
    void exec(Scanner scanner);

    String getKey();

    String getTip();
}
