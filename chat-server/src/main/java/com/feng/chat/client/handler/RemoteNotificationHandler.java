package com.feng.chat.client.handler;

import com.feng.chat.client.session.LocalSession;
import com.feng.chat.client.session.SessionManager;
import com.feng.common.constants.ServerConstants;
import com.feng.common.entity.ImNode;
import com.feng.common.msg.Notification;
import com.feng.common.msg.ProtoMsg;
import com.feng.common.util.JsonUtil;
import com.google.gson.reflect.TypeToken;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Description TODO
 * @Author fengsy
 * @Date 9/29/21
 */
@Slf4j
@Service("RemoteNotificationHandler")
@ChannelHandler.Sharable
public class RemoteNotificationHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (null == msg || !(msg instanceof ProtoMsg.Message)) {
            super.channelRead(ctx, msg);
            return;
        }

        ProtoMsg.Message pkg = (ProtoMsg.Message) msg;

        //取得请求类型,如果不是通知消息，直接跳过
        ProtoMsg.HeadType headType = pkg.getType();
        log.info("收到消息headType：" + headType + ", content: " + pkg.getMessageRequest().getContent());

        if (!headType.equals(ProtoMsg.HeadType.MESSAGE_NOTIFICATION)) {
            super.channelRead(ctx, msg);
            return;
        }

        //处理消息的内容
        ProtoMsg.MessageNotification notificationPkg = pkg.getNotification();
        String json = notificationPkg.getJson();

        log.info("收到通知, json={}", json);
        Notification<Notification.ContentWrapper> notification =
                (Notification<Notification.ContentWrapper>) JsonUtil.jsonToPojo(json, new TypeToken<Notification<Notification.ContentWrapper>>() {
                }.getRawType());

        //下线的通知
        if (notification.getType() == Notification.SESSION_OFF) {
            String sid = notification.getWrapperContent();
            log.info("收到用户下线通知, sid={}", sid);
            SessionManager.getSingletonInstance().removeRemoteSession(sid);
        }
        //上线的通知
        if (notification.getType() == Notification.SESSION_ON) {
            String sid = notification.getWrapperContent();
            log.info("收到用户上线通知, sid={}", sid);

            //待开发
//            SessionManger.inst().addRemoteSession(remoteSession);
        }


        //节点的链接成功
        if (notification.getType() == Notification.CONNECT_FINISHED) {

            Notification<ImNode> nodInfo =
                    (Notification<ImNode>) JsonUtil.jsonToPojo(json, new TypeToken<Notification<ImNode>>() {
                    }.getRawType());


            log.info("收到分布式节点连接成功通知, node={}", json);

            //ctx.pipeline().remove("loginRequest");
            ctx.pipeline().remove("login");
            ctx.channel().attr(ServerConstants.CHANNEL_NAME).set(JsonUtil.pojoToJson(nodInfo));
        }


    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LocalSession session = LocalSession.getSession(ctx);
        if (null != session) {
            session.unbind();
        }
    }
}
