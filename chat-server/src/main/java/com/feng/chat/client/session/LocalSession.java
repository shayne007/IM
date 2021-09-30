package com.feng.chat.client.session;

import com.feng.common.constants.ServerConstants;
import com.feng.common.msg.UserDTO;
import com.feng.common.util.JsonUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @Description socket session实现
 * @Author fengsy
 * @Date 9/29/21
 */
@Slf4j
@Data
public class LocalSession implements ServerSession {
    public static final AttributeKey<String> KEY_USER_ID = AttributeKey.valueOf("key_user_id");

    public static final AttributeKey<LocalSession> SESSION_KEY = AttributeKey.valueOf("SESSION_KEY");
    // 通道
    private Channel channel;
    // 用户
    private UserDTO user;

    // session唯一标示
    private final String sessionId;

    // 登录状态
    private boolean isLogin = false;

    /**
     * session中存储的session 变量属性值
     */
    private Map<String, Object> map = new HashMap<String, Object>();

    public LocalSession(Channel channel) {
        this.channel = channel;
        this.sessionId = buildNewSessionId();
    }

    //反向导航
    public static LocalSession getSession(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        return channel.attr(LocalSession.SESSION_KEY).get();
    }

    private static String buildNewSessionId() {
        String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("-", "");
    }

    // 和channel 通道实现双向绑定
    public LocalSession bind() {
        log.info(" LocalSession 绑定会话 " + channel.remoteAddress());
        channel.attr(LocalSession.SESSION_KEY).set(this);
        channel.attr(ServerConstants.CHANNEL_NAME).set(JsonUtil.pojoToJson(user));
        isLogin = true;
        return this;
    }

    public LocalSession unbind() {
        isLogin = false;
        SessionManager.getSingletonInstance().removeSession(getSessionId());
        this.close();
        return this;
    }

    // 关闭连接
    public synchronized void close() {
        // 用户下线 通知其他节点

        ChannelFuture future = channel.close();
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    log.error("CHANNEL_CLOSED error ");
                }
            }
        });
    }

    //写Protobuf数据帧
    public synchronized void writeAndClose(Object pkg) {
        channel.writeAndFlush(pkg);
        close();
    }

    @Override
    public synchronized void writeAndFlush(Object pkg) {
        if (channel.isWritable()) //低水位
        {
            channel.writeAndFlush(pkg);
        } else {   //高水位时
            log.debug("通道很忙，消息被暂存了");
            //写入消息暂存的分布式存储，如果mongo
            //等channel空闲之后，再写出去
        }
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public boolean isValid() {
        return getUser() != null ? true : false;
    }

    @Override
    public String getUserId() {
        return user.getUserId();
    }
}
