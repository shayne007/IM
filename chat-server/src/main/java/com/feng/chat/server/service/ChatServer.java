package com.feng.chat.server.service;

import com.feng.chat.common.concurrent.FutureTaskScheduler;
import com.feng.chat.common.msg.proto.ProtoMsg;
import com.feng.chat.common.util.IOUtil;
import com.feng.chat.server.distribute.ImWorker;
import com.feng.chat.server.distribute.WorkerRouter;
import com.feng.chat.server.handler.ChatRedirectHandler;
import com.feng.chat.server.handler.LoginRequestHandler;
import com.feng.chat.server.handler.RemoteNotificationHandler;
import com.feng.chat.server.handler.ServerExceptionHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;

/**
 * @Description netty 实现的chat server
 * @Author fengsy
 * @Date 9/29/21
 */
@Slf4j
@Service("ChatServer")
public class ChatServer {

    @Value("${server.port}")
    private int port;
    private EventLoopGroup boss;
    private EventLoopGroup worker;

    private ServerBootstrap bootstrap = new ServerBootstrap();

    @Autowired
    private LoginRequestHandler loginRequestHandler;

    @Autowired
    private ServerExceptionHandler serverExceptionHandler;

    @Autowired
    private ChatRedirectHandler chatRedirectHandler;

    @Autowired
    private RemoteNotificationHandler remoteNotificationHandler;

    public ChatServer() {
    }

    public void run() {

        boss = new NioEventLoopGroup(1);
        worker = new NioEventLoopGroup();
        bootstrap.group(boss, worker);
        bootstrap.channel(NioServerSocketChannel.class);

        String ip = IOUtil.getHostAddress();
        bootstrap.localAddress(new InetSocketAddress(ip, port));
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline p = ch.pipeline();
                p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
                p.addLast("decoder", new ProtobufDecoder(ProtoMsg.Message.getDefaultInstance()));
                p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
                p.addLast("encoder", new ProtobufEncoder());
                p.addLast("login", loginRequestHandler);
                p.addLast("chatRedirect", chatRedirectHandler);
                p.addLast("remoteNotification", remoteNotificationHandler);
                p.addLast("serverException", serverExceptionHandler);

            }
        });

        ChannelFuture channelFuture = null;
        boolean isStart = false;
        while (!isStart) {
            try {
                channelFuture = bootstrap.bind().sync();
                log.info("IM Server启动, 端口为： " + channelFuture.channel().localAddress());
                isStart = true;
            } catch (Exception e) {
                log.error("发生启动异常", e);
                port++;
                log.info("尝试一个新的端口：" + port);
                bootstrap.localAddress(new InetSocketAddress(port));
            }
        }

        ImWorker.getInst().setLocalNode(ip, port);

        FutureTaskScheduler.add(() -> {
            ImWorker.getInst().init();
            WorkerRouter.getInst().init();
        });

    }

}
