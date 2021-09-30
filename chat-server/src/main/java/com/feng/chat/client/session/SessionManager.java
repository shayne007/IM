package com.feng.chat.client.session;

import com.feng.chat.client.distribute.ImWorker;
import com.feng.chat.client.distribute.OnlineCounter;
import com.feng.chat.client.distribute.WorkerRouter;
import com.feng.chat.client.session.dao.SessionCacheDAO;
import com.feng.chat.client.session.dao.UserCacheDAO;
import com.feng.chat.client.session.entity.SessionCache;
import com.feng.chat.client.session.entity.UserCache;
import com.feng.common.entity.ImNode;
import com.feng.common.msg.Notification;
import com.feng.common.util.JsonUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description SessionManager 通过spring容器保证单例
 * @Author fengsy
 * @Date 9/29/21
 */
@Slf4j
@Repository("SessionManager")
public class SessionManager {
    @Autowired
    UserCacheDAO userCacheDAO;

    @Autowired
    SessionCacheDAO sessionCacheDAO;

    private static SessionManager singleInstance = null;

    private ConcurrentHashMap<String, ServerSession> sessionMap = new ConcurrentHashMap();

    public static SessionManager getSingletonInstance() {
        return singleInstance;
    }

    public static void setSingleInstance(SessionManager singleInstance) {
        SessionManager.singleInstance = singleInstance;
    }

    /**
     * 登录成功之后， 增加session对象
     */
    public void addLocalSession(LocalSession session) {
        //step1: 保存本地的session 到会话清单
        String sessionId = session.getSessionId();
        sessionMap.put(sessionId, session);


        String uid = session.getUser().getUserId();


        //step2: 缓存session到redis
        ImNode node = ImWorker.getInst().getLocalNode();
        SessionCache sessionCache = new SessionCache(sessionId, uid, node);
        sessionCacheDAO.save(sessionCache);

        //step3:增加用户的session 信息到用户缓存
        userCacheDAO.addSession(uid, sessionCache);

        //step4: 增加用户数
        OnlineCounter.getInst().increment();
        log.info("本地session增加：{},  在线总数:{} ",
                JsonUtil.pojoToJson(session.getUser()),
                OnlineCounter.getInst().getCurValue());
        ImWorker.getInst().incLoad();

        notifyOtherImNodeOnLine(session);
    }

    /**
     * 根据用户id，获取session对象
     */
    public List<ServerSession> getSessionsBy(String userId) {
        //分布式：分布式保存user和所有session，根据 sessionId 删除用户的会话
        UserCache user = userCacheDAO.get(userId);
        if (null == user) {
            log.info("用户：{} 下线了? 没有在缓存中找到记录 ", userId);
            return null;
        }
        Map<String, SessionCache> allSession = user.getMap();
        if (null == allSession || allSession.size() == 0) {
            log.info("用户：{} 下线了? 没有任何会话 ", userId);
            return null;
        }
        List<ServerSession> sessions = new LinkedList<>();
        allSession.values().stream().forEach(sessionCache ->
        {
            String sid = sessionCache.getSessionId();
            //在本地，取得session
            ServerSession session = sessionMap.get(sid);
            //没有命中，创建远程的session，加入会话集合
            if (session == null) {
                session = new RemoteSession(sessionCache);
                sessionMap.put(sid, session);
            }
            sessions.add(session);
        });
        return sessions;

    }

    // 关闭连接
    public void closeSession(ChannelHandlerContext ctx) {

        LocalSession session = ctx.channel().attr(LocalSession.SESSION_KEY).get();

        if (null == session || !session.isValid()) {
            log.error("session is null or isValid");
            return;
        }

        session.close();
        // 删除本地的会话和远程会话
        this.removeSession(session.getSessionId());

        /**
         * 通知其他节点 ，用户下线
         */
        notifyOtherImNodeOffLine(session);

    }

    /**
     * 通知其他节点 有会话下线
     *
     * @param session session
     */
    private void notifyOtherImNodeOffLine(LocalSession session) {

        if (null == session || session.isValid()) {
            log.error("session is null or isValid");
            return;
        }

        int type = Notification.SESSION_OFF;

        Notification<Notification.ContentWrapper> notification = Notification.wrapContent(session.getSessionId());
        notification.setType(type);
        WorkerRouter.getInst().sendNotification(JsonUtil.pojoToJson(notification));
    }

    /**
     * 通知其他节点 有会话上线
     *
     * @param session session
     */
    private void notifyOtherImNodeOnLine(LocalSession session) {
        int type = Notification.SESSION_ON;
        Notification<Notification.ContentWrapper> notification = Notification.wrapContent(session.getSessionId());
        notification.setType(type);
        WorkerRouter.getInst().sendNotification(JsonUtil.pojoToJson(notification));
    }

    public void removeSession(String sessionId) {
        if (!sessionMap.containsKey(sessionId)) {
            return;
        }
        ServerSession session = sessionMap.get(sessionId);
        String uid = session.getUserId();
        // 减少用户数
        OnlineCounter.getInst().decrement();
        log.info("本地session减少：{} 下线了,  在线总数:{} ", uid, OnlineCounter.getInst().getCurValue());
        ImWorker.getInst().decrLoad();
        // 分布式：分布式保存user和所有session，根据 sessionId 删除用户的会话
        userCacheDAO.removeSession(uid, sessionId);

        // step2:删除缓存session
        sessionCacheDAO.remove(sessionId);

        // 本地：从会话集合中，删除会话
        sessionMap.remove(sessionId);

    }

    /**
     * 远程用户下线，数据堆积，删除session
     */
    public void removeRemoteSession(String sessionId) {
        if (!sessionMap.containsKey(sessionId)) {
            return;
        }
        sessionMap.remove(sessionId);
    }
}
