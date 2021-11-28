package com.feng.chat.server.session.entity;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Description 用户缓存对象，包含用户的userId、与该用户关联的session映射表
 * @Author fengsy
 * @Date 9/29/21
 */
@Data
public class UserCache {
    private String userId;
    private Map<String, SessionCache> map = new LinkedHashMap<>(10);

    public UserCache(String userId) {
        this.userId = userId;
    }

    public void addSession(SessionCache session) {
        map.put(session.getSessionId(), session);
    }

    public void removeSession(String sessionId) {
        map.remove(sessionId);
    }
}
