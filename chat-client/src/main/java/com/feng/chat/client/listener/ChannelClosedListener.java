package com.feng.chat.client.listener;

import com.feng.chat.client.service.CommandManager;
import com.feng.chat.client.session.ClientSession;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @Description 通道关闭监听器
 * @Author fengsy
 * @Date 10/1/21
 */
@Slf4j
public class ChannelClosedListener implements GenericFutureListener<ChannelFuture> {
    private CommandManager manager;

    public ChannelClosedListener(CommandManager manager) {
        this.manager = manager;
    }

    @Override
    public void operationComplete(ChannelFuture f) throws Exception {
        log.info(new Date() + ": 连接已经断开……");
        manager.setChannel(f.channel());

        // 创建会话
        ClientSession session = manager.getChannel().attr(ClientSession.SESSION_KEY).get();
        session.close();

        manager.setConnectFlag(false);
        //唤醒用户线程
        manager.notifyCommandThread();
    }
}
