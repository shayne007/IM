package com.feng.chat.server.handler;

import com.feng.chat.common.concurrent.FutureTaskScheduler;
import com.feng.chat.common.msg.proto.ProtoMsg;
import com.feng.chat.server.processor.ChatRedirectProcessor;
import com.feng.chat.server.session.LocalSession;
import com.feng.chat.server.session.ServerSession;
import com.feng.chat.server.session.SessionManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description 聊天消息的转发 入站处理器
 * @Author fengsy
 * @Date 9/30/21
 */
@Slf4j
@Service("ChatRedirectHandler")
@ChannelHandler.Sharable
public class ChatRedirectHandler extends ChannelInboundHandlerAdapter {
    @Autowired
    ChatRedirectProcessor redirectProcesser;

    @Autowired
    SessionManager sessionManger;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //判断消息实例
        if (null == msg || !(msg instanceof ProtoMsg.Message)) {
            super.channelRead(ctx, msg);
            return;
        }
        //判断消息类型
        ProtoMsg.Message pkg = (ProtoMsg.Message) msg;
        ProtoMsg.HeadType headType = ((ProtoMsg.Message) msg).getType();
        log.info("收到消息headType：" + headType + ", content: " + pkg.getMessageRequest().getContent());

        if (!headType.equals(redirectProcesser.msgType())) {
            super.channelRead(ctx, msg);
            return;
        }
        //异步处理转发的逻辑
        FutureTaskScheduler.add(() ->
        {
            //判断是否登录,如果登录了，则为用户消息
            LocalSession session = LocalSession.getSession(ctx);
            if (null != session && session.isLogin()) {

                redirectProcesser.action(session, pkg);
                return;
            }

            //没有登录，则为中转消息
            ProtoMsg.MessageRequest request = pkg.getMessageRequest();
            List<ServerSession> toSessions = SessionManager.getSingletonInstance().getSessionsBy(request.getTo());
            toSessions.forEach((serverSession) ->
            {

                if (serverSession instanceof LocalSession)
                // 将IM消息发送到接收方
                {
                    log.info("发送中转消息给：" + serverSession.getUserId());
                    serverSession.writeAndFlush(pkg);

                }

            });


        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LocalSession session = ctx.channel().attr(LocalSession.SESSION_KEY).get();

        if (null != session && session.isValid()) {
            session.close();
            sessionManger.removeSession(session.getSessionId());
        }
    }
}
