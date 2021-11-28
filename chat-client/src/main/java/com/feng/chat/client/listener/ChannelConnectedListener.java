package com.feng.chat.client.listener;

import com.feng.chat.client.service.CommandManager;
import com.feng.chat.client.session.ClientSession;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @Description channel socket连接成功 监听器
 * @Author fengsy
 * @Date 10/1/21
 */
@Slf4j
@Data
public class ChannelConnectedListener implements GenericFutureListener<ChannelFuture> {

    private CommandManager manager;

    public ChannelConnectedListener(CommandManager manager) {
        this.manager = manager;
    }

    @Override
    public void operationComplete(ChannelFuture f) throws Exception {
        final EventLoop eventLoop = f.channel().eventLoop();
        boolean connectFlag = false;
        if (!f.isSuccess() && (++manager.reConnectCount) < 3) {
            log.info("连接失败! 在10s之后准备尝试第{}次重连!", manager.reConnectCount);
            eventLoop.schedule(() -> manager.getNettyClient().doConnect(), 10, TimeUnit.SECONDS);
            connectFlag = false;
        } else if (f.isSuccess()) {
            connectFlag = true;
            log.info("IM 服务器 连接成功!");
            Channel channel = f.channel();
            manager.setChannel(channel);
            // 创建会话
            ClientSession session = new ClientSession(channel);
            session.setConnected(true);
            manager.setSession(session);
            manager.getChannel().closeFuture().addListener(new ChannelClosedListener(manager));
            //唤醒用户线程
            manager.notifyCommandThread();

        } else {
            log.info("IM 服务器 多次连接失败!");
            connectFlag = false;
            //唤醒用户线程
            manager.notifyCommandThread();
        }
        manager.setConnectFlag(connectFlag);
    }

}
