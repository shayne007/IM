package com.feng.chat.server.handler;

import com.feng.chat.common.concurrent.FutureTaskScheduler;
import com.feng.chat.common.constants.ServerConstants;
import com.feng.chat.common.msg.proto.ProtoMsg;
import com.feng.chat.server.session.SessionManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @Description 服务端 心跳检测处理器
 * @Author fengsy
 * @Date 9/30/21
 */
@Slf4j
public class HeartBeatServerHandler extends IdleStateHandler {

    private static final int READ_IDLE_GAP_SEC = 1500;

    public HeartBeatServerHandler() {
        super(READ_IDLE_GAP_SEC, 0, 0, TimeUnit.SECONDS);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //判断消息实例
        if (null == msg || !(msg instanceof ProtoMsg.Message)) {
            super.channelRead(ctx, msg);
            return;
        }
        ProtoMsg.Message pkg = (ProtoMsg.Message) msg;
        //判断消息类型
        ProtoMsg.HeadType headType = pkg.getType();
        log.info("收到消息headType：" + headType + ", content: " + pkg.getMessageRequest().getContent());

        if (headType.equals(ProtoMsg.HeadType.HEART_BEAT)) {
            //异步处理,将心跳包，直接回复给客户端
            FutureTaskScheduler.add(() ->
            {
                if (ctx.channel().isActive()) {
                    System.out.println("回写心跳包：" + msg.toString());
                    ctx.writeAndFlush(msg);
                }
            });
        }
        super.channelRead(ctx, msg);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        log.info(READ_IDLE_GAP_SEC + "秒内未读到数据，关闭连接", ctx.channel().attr(ServerConstants.CHANNEL_NAME).get());
        SessionManager.getSingletonInstance().closeSession(ctx);
    }
}
