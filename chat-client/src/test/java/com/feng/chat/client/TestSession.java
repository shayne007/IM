package com.feng.chat.client;

import com.feng.chat.client.sender.ChatSender;
import com.feng.chat.client.session.ClientSession;
import com.feng.chat.common.msg.UserDTO;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

/**
 * @Description 客户端session测试用例
 * @Author fengsy
 * @Date 10/2/21
 */
public class TestSession {

    @Test
    public void sendChatMsg() {
        ChatSender sender = new ChatSender();
        UserDTO user = new UserDTO();
        user.setUserId("1");
        user.setNickName("张三");
        user.setSessionId("-1");
        ClientSession session = new ClientSession(new EmbeddedChannel());
        session.setConnected(true);
        sender.setSession(session);
        sender.setUser(user);

        sender.sendChatMsg("dd", "1");

        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSendLoginMsg() {

    }
}
