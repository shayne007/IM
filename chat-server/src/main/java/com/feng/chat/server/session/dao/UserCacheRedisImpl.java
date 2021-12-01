package com.feng.chat.server.session.dao;

import com.feng.chat.common.util.GsonUtil;
import com.feng.chat.server.session.entity.SessionCache;
import com.feng.chat.server.session.entity.UserCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * @Description 用户缓存 基于redis实现
 * @Author fengsy
 * @Date 9/29/21
 */
@Repository("UserCacheRedisImpl")
public class UserCacheRedisImpl implements UserCacheDAO {
    public static final String REDIS_PREFIX = "UserCache:uid:";
    //4小时之后，得重新登录
    private static final long CACHE_EXPIRE_MIN = 60 * 4;

    @Autowired
    protected StringRedisTemplate stringRedisTemplate;

    @Override
    public void save(final UserCache userCache) {
        String key = REDIS_PREFIX + userCache.getUserId();
        String value = GsonUtil.pojoToJson(userCache);
        stringRedisTemplate.opsForValue().set(key, value, CACHE_EXPIRE_MIN, TimeUnit.MINUTES);
    }

    @Override
    public UserCache get(String userId) {
        String jsonStr = stringRedisTemplate.opsForValue().get(REDIS_PREFIX + userId);
        UserCache userCache = null;
        if (!StringUtils.isEmpty(jsonStr)) {
            userCache = GsonUtil.jsonToPojo(jsonStr, UserCache.class);
        }
        return userCache;
    }

    @Override
    public void addSession(String uid, SessionCache session) {
        UserCache userCache = get(uid);
        if (null == userCache) {
            userCache = new UserCache(uid);
        }
        userCache.addSession(session);
        save(userCache);
    }

    @Override
    public void removeSession(String uid, String sessionId) {
        UserCache userCache = get(uid);
        if (null == userCache) {
            userCache = new UserCache(uid);
        }
        userCache.removeSession(sessionId);
        save(userCache);
    }
}
