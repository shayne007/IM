package com.feng.chat.client.session.entity;

import com.feng.common.entity.ImNode;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;

/**
 * @Description session缓存对象
 * @Author fengsy
 * @Date 9/29/21
 */
@Data
public class SessionCache implements Serializable {
    private static final long serialVersionUID = -403010884211394856L;

    private String userId;
    @Getter
    private String sessionId;

    private ImNode imNode;

    public SessionCache() {
        userId = "";
        sessionId = "";
        imNode = new ImNode("unKnown", 0);
    }

    public SessionCache(String sessionId, String userId, ImNode imNode) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.imNode = imNode;
    }
}
