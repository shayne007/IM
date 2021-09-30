package com.feng.chat.client.distribute;

import com.feng.chat.client.handler.ImNodeExceptionHandler;
import com.feng.chat.client.handler.ImNodeHeartBeatClientHandler;
import com.feng.chat.client.protoBuilder.NotificationMsgBuilder;
import com.feng.common.entity.ImNode;
import com.feng.common.msg.Notification;
import com.feng.common.msg.ProtoMsg;
import com.feng.common.msg.UserDTO;
import com.feng.common.util.JsonUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @Description server节点间的消息转发器, 开启netty客户端socket连接进行消息转发
 * @Author fengsy
 * @Date 9/29/21
 */
@Slf4j
@Data
public class PeerSender {
    private int reConnectCount = 0;
    private static final int MAX_RETRY_COUNT = 3;

    private Channel channel;

    private ImNode remoteNode;

    private boolean connectFlag = false;

    private UserDTO user;

    private GenericFutureListener<ChannelFuture> closedListener = (ChannelFuture f) ->
    {
        log.info("分布式连接已经断开……{}", remoteNode.toString());
        channel = null;
        connectFlag = false;
    };
    private GenericFutureListener<ChannelFuture> connectedListener = (ChannelFuture f) ->
    {
        final EventLoop eventLoop = f.channel().eventLoop();
        if (!f.isSuccess() && ++reConnectCount < MAX_RETRY_COUNT) {
            log.info("连接失败! 在10s之后准备尝试第{}次重连!", reConnectCount);
            eventLoop.schedule(() -> PeerSender.this.doConnect(), 10, TimeUnit.SECONDS);

            connectFlag = false;
        } else {
            connectFlag = true;

            log.info(new Date() + "分布式节点连接成功:{}", remoteNode.toString());

            channel = f.channel();
            channel.closeFuture().addListener(closedListener);

            /**
             * 发送链接成功的通知
             */
            Notification<ImNode> notification = new Notification<>(ImWorker.getInst().getLocalNode());
            notification.setType(Notification.CONNECT_FINISHED);
            String json = JsonUtil.pojoToJson(notification);
            ProtoMsg.Message pkg = NotificationMsgBuilder.buildNotification(json);
            writeAndFlush(pkg);
        }
    };

    private Bootstrap bootstrap;
    private EventLoopGroup group;

    public PeerSender(ImNode node) {
        this.remoteNode = node;
        bootstrap = new Bootstrap();
        group = new NioEventLoopGroup();
    }

    public void doConnect() {
        String host = remoteNode.getHost();
        int port = remoteNode.getPort();

        if (bootstrap != null && bootstrap.config() == null) {
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            bootstrap.remoteAddress(host, port);

            // 设置通道初始化
            bootstrap.handler(
                    new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast("decoder", new ProtobufDecoder(ProtoMsg.Message.getDefaultInstance()));
                            ch.pipeline().addLast("encoder", new ProtobufEncoder());
                            ch.pipeline().addLast("imNodeHeartBeatClientHandler", new ImNodeHeartBeatClientHandler());
                            ch.pipeline().addLast("exceptionHandler", new ImNodeExceptionHandler());
                        }
                    }
            );

            log.info(new Date() + "开始连接分布式节点:{}", remoteNode.toString());

            ChannelFuture f = bootstrap.connect();
            f.addListener(connectedListener);

        } else if (bootstrap.config() != null) {
            log.info(new Date() + "再一次开始连接分布式节点", remoteNode.toString());
            ChannelFuture f = bootstrap.connect();
            f.addListener(connectedListener);
        }
    }

    public void stopConnecting() {
        group.shutdownGracefully();
        connectFlag = false;
    }

    public void writeAndFlush(Object msg) {
        ProtoMsg.Message pkg = (ProtoMsg.Message) msg;

        //取得请求类型,如果不是通知消息，直接跳过
        ProtoMsg.HeadType headType = pkg.getType();
        log.info("PeerSender writting... " + headType);
        if (connectFlag == false) {
            log.error("分布式节点未连接:", remoteNode.toString());
            return;
        }
        channel.writeAndFlush(pkg);
    }
}

