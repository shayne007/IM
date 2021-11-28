package com.feng.chat.client.service;

import com.feng.chat.client.handler.ChatMsgHandler;
import com.feng.chat.client.handler.ExceptionHandler;
import com.feng.chat.client.handler.LoginResponseHandler;
import com.feng.chat.common.msg.proto.ProtoMsg;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description netty客户端，与server建立socket连接
 * @Author fengsy
 * @Date 9/30/21
 */
@Slf4j
@Data
@Service("NettyClient")
public class NettyClient {
    // 服务器ip地址
    private String host;
    // 服务器端口
    private int port;


    @Autowired
    private ChatMsgHandler chatMsgHandler;

    @Autowired
    private LoginResponseHandler loginResponseHandler;


    @Autowired
    private ExceptionHandler exceptionHandler;

    private boolean initFalg = true;

    private GenericFutureListener<ChannelFuture> connectedListener;

    private Bootstrap bootstrap;
    private EventLoopGroup group;

    public NettyClient() {
        group = new NioEventLoopGroup();
    }

    /**
     * 重连
     */
    public void doConnect() {
        try {
            bootstrap = new Bootstrap();

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
                            ch.pipeline().addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
                            ch.pipeline().addLast("decoder", new ProtobufDecoder(ProtoMsg.Message.getDefaultInstance()));
                            ch.pipeline().addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
                            ch.pipeline().addLast("encoder", new ProtobufEncoder());
                            ch.pipeline().addLast("loginResponseHandler", loginResponseHandler);
                            ch.pipeline().addLast("chatMsgHandler", chatMsgHandler);
                            ch.pipeline().addLast("exceptionHandler", exceptionHandler);
                        }
                    }
            );
            log.info("客户端开始连接 [IM Server]");

            ChannelFuture f = bootstrap.connect();
            f.addListener(connectedListener);


            // 阻塞
            // f.channel().closeFuture().sync();

        } catch (Exception e) {
            log.info("客户端连接失败!" + e.getMessage());
        }
    }
}
