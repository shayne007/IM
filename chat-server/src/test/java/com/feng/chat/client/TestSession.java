package com.feng.chat.client;

import com.feng.chat.common.msg.UserDTO;
import com.feng.chat.common.util.GsonUtil;
import com.feng.chat.common.util.SpringContextUtil;
import com.feng.chat.server.ChatServerApplication;
import com.feng.chat.server.session.LocalSession;
import com.feng.chat.server.session.ServerSession;
import com.feng.chat.server.session.SessionManager;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @Description 客户端session测试用例
 * @Author fengsy
 * @Date 10/2/21
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ChatServerApplication.class)
public class TestSession {

    @Test
    public void testSession() {
        /**
         * 将SessionManger 单例设置为spring bean
         */
        SessionManager sessionManger = SpringContextUtil.getBean(SessionManager.class);
        sessionManger.setSingleInstance(sessionManger);

        UserDTO user = new UserDTO();
        user.setUserId("2");
        String userStr = GsonUtil.pojoToJson(user);
        System.out.println(userStr);
        for (int i = 0; i < 10; i++) {
            LocalSession session = new LocalSession(new EmbeddedChannel());
            session.setUser(user);
            session.bind();
            sessionManger.addLocalSession(session);
        }
        List<ServerSession> sessions = SessionManager.getSingletonInstance().getSessionsBy(user.getUserId());
        sessions.stream().forEach(s -> System.out.println(s.getSessionId()));

    }
}
