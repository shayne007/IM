package com.feng.chat.client.sender;

import com.feng.chat.client.session.ClientSession;
import com.feng.chat.common.concurrent.CallbackTask;
import com.feng.chat.common.concurrent.CallbackTaskScheduler;
import com.feng.chat.common.msg.UserDTO;
import com.feng.chat.common.msg.proto.ProtoMsg;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public abstract class BaseSender {


    private UserDTO user;
    private ClientSession session;

    public boolean isConnected() {
        if (null == session) {
            log.info("session is null");
            return false;
        }

        return session.isConnected();
    }

    public boolean isLogin() {
        if (null == session) {
            log.info("session is null");
            return false;
        }

        return session.isLogin();
    }

    /**
     * 异步发送消息
     *
     * @param message
     */
    public void sendMsg(ProtoMsg.Message message) {
        CallbackTaskScheduler.add(new CallbackTask<Boolean>() {
            @Override
            public Boolean execute() throws Exception {
                if (null == getSession()) {
                    throw new Exception("session is null");
                }

                if (!isConnected()) {
                    log.info("连接还没成功");
                    throw new Exception("连接还没成功");
                }

                final Boolean[] isSuccess = {false};

                ChannelFuture f = getSession().witeAndFlush(message);
                f.addListener(new GenericFutureListener<Future<? super Void>>() {
                    @Override
                    public void operationComplete(Future<? super Void> future)
                            throws Exception {
                        // 回调
                        if (future.isSuccess()) {
                            isSuccess[0] = true;

                            log.info("操作成功");
                        }
                    }

                });


                try {
                    f.sync();
                } catch (InterruptedException e) {
                    isSuccess[0] = false;
                    e.printStackTrace();
                    throw new Exception("error occur");
                }

                return isSuccess[0];
            }

            @Override
            public void onBack(Boolean b) {
                if (b) {
                    BaseSender.this.sendSucced(message);

                } else {
                    BaseSender.this.sendfailed(message);

                }

            }

            @Override
            public void onException(Throwable t) {
                BaseSender.this.sendException(message);
            }
        });


    }

    protected void sendSucced(ProtoMsg.Message message) {
        log.info("发送成功");

    }

    protected void sendfailed(ProtoMsg.Message message) {
        log.info("发送失败");
    }

    protected void sendException(ProtoMsg.Message message) {
        log.info("发送消息出现异常");

    }

}
