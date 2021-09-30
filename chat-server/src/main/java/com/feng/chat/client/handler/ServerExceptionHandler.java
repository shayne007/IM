package com.feng.chat.client.handler;

import com.feng.chat.client.session.SessionManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Description 服务端异常处理 入站处理器
 * @Author fengsy
 * @Date 9/29/21
 */
@Slf4j
@ChannelHandler.Sharable
@Service("ServerExceptionHandler")
public class ServerExceptionHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 捕捉异常信息
        cause.printStackTrace();
        log.error(cause.getMessage());

        SessionManager.getSingletonInstance().closeSession(ctx);
        ctx.close();
    }
}
