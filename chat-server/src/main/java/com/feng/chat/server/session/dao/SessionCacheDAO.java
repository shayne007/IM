package com.feng.chat.server.session.dao;

import com.feng.chat.server.session.entity.SessionCache;

/**
 * @author fengsy
 * @Description session缓存，接口定义
 * @date 9/29/21
 */
public interface SessionCacheDAO {
    /**
     * 保存 会话 到  缓存
     *
     * @param s
     */
    void save(SessionCache s);

    /**
     * 从缓存 获取  会话
     *
     * @param sessionId
     * @return
     */
    SessionCache get(String sessionId);

    /**
     * 删除会话
     *
     * @param sessionId
     */
    void remove(String sessionId);
}
