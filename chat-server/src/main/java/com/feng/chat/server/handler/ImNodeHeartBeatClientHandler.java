package com.feng.chat.server.handler;

import com.feng.chat.common.msg.proto.ProtoMsg;
import com.feng.chat.common.util.GsonUtil;
import com.feng.chat.server.distribute.ImWorker;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @Description 服务端server集群节点间的心跳检测处理器
 * @Author fengsy
 * @Date 9/29/21
 */
@Slf4j
@ChannelHandler.Sharable
public class ImNodeHeartBeatClientHandler extends ChannelInboundHandlerAdapter {
    //心跳的时间间隔，单位为s
    private static final int HEARTBEAT_INTERVAL_SEC = 50;

    String from = null;
    int seq = 0;


    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        //发送心跳
        heartBeat(ctx);
    }

    private void heartBeat(ChannelHandlerContext ctx) {
        ProtoMsg.Message message = buildHeartBeatMsg();
        ctx.executor().schedule(() -> {
            if (ctx.channel().isActive()) {
                log.info("发送 imNode HEATBEAT 消息");
                ctx.writeAndFlush(message);
                heartBeat(ctx);
            }
        }, HEARTBEAT_INTERVAL_SEC, TimeUnit.SECONDS);
    }

    private ProtoMsg.Message buildHeartBeatMsg() {
        if (null == from) {
            from = GsonUtil.pojoToJson(ImWorker.getInst().getLocalNode());
        }

        ProtoMsg.Message.Builder mb = ProtoMsg.Message.newBuilder()
                .setType(ProtoMsg.HeadType.HEART_BEAT)  //设置消息类型
                .setSequence(++seq);                 //设置应答流水，与请求对应
        ProtoMsg.MessageHeartBeat.Builder heartBeat = ProtoMsg.MessageHeartBeat.newBuilder()
                .setSeq(seq)
                .setJson(from)
                .setUid("-1");
        mb.setHeartBeat(heartBeat.build());
        return mb.build();
    }

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
            ProtoMsg.MessageHeartBeat messageHeartBeat = pkg.getHeartBeat();
            log.info("  收到 imNode HEART_BEAT  消息 from: " + messageHeartBeat.getJson());
            log.info("  收到 imNode HEART_BEAT seq: " + messageHeartBeat.getSeq());

            return;
        } else {
            super.channelRead(ctx, msg);

        }
    }
}
