package com.feng.chat.client.processor;

import com.feng.chat.client.session.LocalSession;
import io.netty.channel.Channel;

/**
 * @Description TODO
 * @Author fengsy
 * @Date 9/30/21
 */
public abstract class AbstractServerProcessor implements ServerReciever {

    protected String getKey(Channel ch) {
        return ch.attr(LocalSession.KEY_USER_ID).get();
    }

    protected void setKey(Channel ch, String key) {
        ch.attr(LocalSession.KEY_USER_ID).set(key);
    }

    protected void checkAuth(Channel ch) throws Exception {
        if (null == getKey(ch)) {
            throw new Exception("此用户，没有登录成功");
        }


    }
}
