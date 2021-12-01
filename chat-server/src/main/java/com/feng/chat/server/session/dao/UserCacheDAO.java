package com.feng.chat.server.session.dao;

import com.feng.chat.server.session.entity.SessionCache;
import com.feng.chat.server.session.entity.UserCache;

/**
 * @Description 用户缓存，接口定义
 * @Author fengsy
 * @Date 9/29/21
 */
public interface UserCacheDAO {
    /**
     * 保存用户缓存
     */
    void save(UserCache s);

    /**
     * 获取用户缓存
     *
     * @param userId
     * @return
     */
    UserCache get(String userId);

    /**
     * 增加 用户的  会话
     *
     * @param uid
     * @param session
     */
    void addSession(String uid, SessionCache session);

    /**
     * 删除 用户的  会话
     *
     * @param uid
     * @param sessionId
     */
    void removeSession(String uid, String sessionId);
}
