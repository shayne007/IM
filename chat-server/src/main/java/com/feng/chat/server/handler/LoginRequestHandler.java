package com.feng.chat.server.handler;

import com.feng.chat.common.concurrent.CallbackTask;
import com.feng.chat.common.concurrent.CallbackTaskScheduler;
import com.feng.chat.common.msg.proto.ProtoMsg;
import com.feng.chat.server.processor.LoginProcessor;
import com.feng.chat.server.session.LocalSession;
import com.feng.chat.server.session.SessionManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description 登录请求的入站处理器
 * @Author fengsy
 * @Date 9/29/21
 */
@Slf4j
@Service("LoginRequestHandler")
@ChannelHandler.Sharable
public class LoginRequestHandler extends ChannelInboundHandlerAdapter {
    @Autowired
    LoginProcessor loginProcessor;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg == null || !(msg instanceof ProtoMsg.Message)) {
            super.channelRead(ctx, msg);
            return;
        }

        ProtoMsg.Message pkg = (ProtoMsg.Message) msg;
        // 取得请求类型
        ProtoMsg.HeadType headType = pkg.getType();
        log.info("收到消息headType：" + headType + ", content: " + pkg.getMessageRequest().getContent());
        if (!headType.equals(loginProcessor.msgType())) {
            super.channelRead(ctx, msg);
            return;
        }

        LocalSession session = new LocalSession(ctx.channel());
        //异步任务，处理登录的逻辑
        CallbackTaskScheduler.add(new CallbackTask<Boolean>() {
            @Override
            public Boolean execute() throws Exception {
                return loginProcessor.action(session, pkg);
            }

            //异步任务返回
            @Override
            public void onBack(Boolean result) {
                if (result) {
                    log.info("登录成功:" + session.getUser());
                    ctx.pipeline().addAfter("login", "heartBeat", new HeartBeatServerHandler());
                    ctx.pipeline().remove("login");
                } else {
                    SessionManager.getSingletonInstance().closeSession(ctx);
                    log.info("登录失败:" + session.getUser());
                }
            }

            //异步任务异常
            @Override
            public void onException(Throwable t) {
                t.printStackTrace();
                log.info("登录失败:" + session.getUser());
                SessionManager.getSingletonInstance().closeSession(ctx);
            }
        });
    }
}
