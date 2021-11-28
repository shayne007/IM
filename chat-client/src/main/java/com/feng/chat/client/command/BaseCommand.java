package com.feng.chat.client.command;

import java.util.Scanner;

public interface BaseCommand {
    boolean exec(Scanner scanner);

    String getKey();

    String getTip();
}
