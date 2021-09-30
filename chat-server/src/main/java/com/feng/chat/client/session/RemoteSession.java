package com.feng.chat.client.session;

import com.feng.chat.client.distribute.PeerSender;
import com.feng.chat.client.distribute.WorkerRouter;
import com.feng.chat.client.session.entity.SessionCache;
import com.feng.common.entity.ImNode;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * @Description socket session实现
 * @Author fengsy
 * @Date 9/29/21
 */
@Slf4j
public class RemoteSession implements ServerSession, Serializable {
    private static final long serialVersionUID = -400010884211394846L;
    SessionCache cache;

    private boolean valid = true;

    public RemoteSession(SessionCache sessionCache) {
        this.cache = sessionCache;
    }

    @Override
    public void writeAndFlush(Object pkg) {
        ImNode imNode = cache.getImNode();
        long nodeId = imNode.getId();

        log.info("cache: " + cache);
        //获取转发的  sender
        PeerSender sender = WorkerRouter.getInst().route(nodeId);
        log.info("sender: " + sender);
        if (null != sender) {
            sender.writeAndFlush(pkg);
        }
    }

    @Override
    public String getSessionId() {
        return cache.getSessionId();
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public String getUserId() {
        return cache.getUserId();
    }
}
