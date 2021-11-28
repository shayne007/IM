package com.feng.chat.client;

import com.feng.chat.common.util.GsonUtil;
import com.feng.chat.server.ChatServerApplication;
import com.feng.chat.server.session.dao.SessionCacheDAO;
import com.feng.chat.server.session.dao.UserCacheDAO;
import com.feng.chat.server.session.entity.SessionCache;
import com.feng.chat.server.session.entity.UserCache;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @Description Redis测试用例
 * @Author fengsy
 * @Date 10/2/21
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ChatServerApplication.class)
@Slf4j
public class TestRedisService {
    @Resource
    private UserCacheDAO userCacheDAO;

    @Resource
    private SessionCacheDAO sessionCacheDAO;

    @Test
    public void testSaveUser() throws Exception {
        UserCache cache = new UserCache("2");
        SessionCache sessionCache = new SessionCache();
        sessionCache.setSessionId("1");
        sessionCache.setUserId(cache.getUserId());

        cache.addSession(sessionCache);
        userCacheDAO.save(cache);

        UserCache userCache = userCacheDAO.get("2");

        System.out.println("userCache = " + GsonUtil.pojoToJson(userCache));
    }

    @Test
    public void testSaveSession() throws Exception {
        SessionCache cache = new SessionCache();
        cache.setSessionId("1");
        sessionCacheDAO.save(cache);

        SessionCache sessionCache2 = sessionCacheDAO.get("1");

        System.out.println("sessionCache2 = " + sessionCache2);
    }
}
