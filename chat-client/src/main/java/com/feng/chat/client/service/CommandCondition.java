package com.feng.chat.client.service;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Description TODO
 * @Author fengsy
 * @Date 10/1/21
 */
public class CommandCondition {

    private final ReentrantLock lock = new ReentrantLock();

    private Condition noInput = lock.newCondition();

    public void notifyCommandThread() throws InterruptedException {
        noInput.await();
    }

    public void waitCommandThread() {
        noInput.signalAll();
    }

}
