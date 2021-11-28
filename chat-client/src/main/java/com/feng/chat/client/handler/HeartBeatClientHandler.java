package com.feng.chat.client.handler;


import com.feng.chat.client.protoBuilder.HeartBeatMsgBuilder;
import com.feng.chat.client.session.ClientSession;
import com.feng.chat.common.msg.UserDTO;
import com.feng.chat.common.msg.proto.ProtoMsg;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 客户端定时发送心跳包，时间间隔为服务端空闲检测时间间隔的1/3,防止丢包，给客户端重发的机会
 */
@Slf4j
@ChannelHandler.Sharable
@Service("HeartBeatClientHandler")
public class HeartBeatClientHandler extends ChannelInboundHandlerAdapter {
    //心跳的时间间隔，单位为s
    private static final int HEARTBEAT_INTERVAL = 500;

    //在通道被激活时，开始发送心跳
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ClientSession session = ClientSession.getSession(ctx);
        UserDTO user = session.getUser();
        HeartBeatMsgBuilder builder = new HeartBeatMsgBuilder(user, session);

        ProtoMsg.Message message = builder.buildMsg();
        //发送心跳
        heartBeat(ctx, message);
    }

    //使用定时器，发送心跳报文
    private void heartBeat(ChannelHandlerContext ctx, ProtoMsg.Message heartbeatMsg) {
        ctx.executor().schedule(() ->
        {
            if (ctx.channel().isActive()) {
                log.info(" 发送 HEART_BEAT  消息 to server");
                ctx.writeAndFlush(heartbeatMsg);

                //递归调用，发送下一次的心跳
                heartBeat(ctx, heartbeatMsg);
            }
        }, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * 接受到服务器的心跳回写
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //判断消息实例
        if (null == msg || !(msg instanceof ProtoMsg.Message)) {
            super.channelRead(ctx, msg);
            return;
        }
        //判断类型
        ProtoMsg.Message pkg = (ProtoMsg.Message) msg;
        ProtoMsg.HeadType headType = pkg.getType();
        if (headType.equals(ProtoMsg.HeadType.HEART_BEAT)) {
            log.info(" 收到回写的 HEART_BEAT  消息 from server");
            return;
        } else {
            super.channelRead(ctx, msg);
        }
    }
}
