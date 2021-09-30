package com.feng.chat.client.session.dao;


import com.feng.chat.client.session.entity.SessionCache;
import com.feng.common.util.JsonUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @Description session缓存 基于redis实现
 * @Author fengsy
 * @Date 9/29/21
 */
@Repository("SessionCacheRedisImpl")
public class SessionCacheRedisImpl implements SessionCacheDAO {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public static final String REDIS_PREFIX = "SessionCache:id:";
    //4小时之后，得重新登录
    private static final long CACHE_EXPIRE_MIN = 60 * 4;

    @Override
    public void save(final SessionCache sessionCache) {
        String key = REDIS_PREFIX + sessionCache.getSessionId();
        String value = JsonUtil.pojoToJson(sessionCache);

        stringRedisTemplate.opsForValue().set(key, value, CACHE_EXPIRE_MIN, TimeUnit.MINUTES);

    }

    @Override
    public SessionCache get(String sessionId) {
        String key = REDIS_PREFIX + sessionId;
        String value = stringRedisTemplate.opsForValue().get(key);

        if (!StringUtils.isEmpty(value)) {
            return JsonUtil.jsonToPojo(value, SessionCache.class);
        }
        return null;
    }

    @Override
    public void remove(String sessionId) {
        String key = REDIS_PREFIX + sessionId;
        stringRedisTemplate.delete(key);
    }
}
