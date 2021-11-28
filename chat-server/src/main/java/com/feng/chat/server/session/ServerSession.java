package com.feng.chat.server.session;

/**
 * @author fengsy
 * @Description
 * @date 9/29/21
 */
public interface ServerSession {
    void writeAndFlush(Object pkg);

    String getSessionId();

    boolean isValid();

    String getUserId();
}
