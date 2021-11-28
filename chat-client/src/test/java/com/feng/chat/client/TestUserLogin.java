package com.feng.chat.client;

import com.feng.chat.client.feign.UserActionClient;
import com.feng.chat.client.feign.WebOperator;
import com.feng.chat.common.entity.LoginBackMsg;
import com.feng.chat.common.msg.UserDTO;
import feign.Feign;
import feign.gson.GsonDecoder;
import org.junit.Test;

/**
 * 远程API的本地调用
 * Created by 尼恩 at 疯狂创客圈
 */

public class TestUserLogin {
    /**
     * 测试登录
     */
    @Test
    public void testLogin() {
        UserActionClient action = Feign.builder()
                .decoder(new GsonDecoder())
                .target(UserActionClient.class, "http://localhost:8080/");
        LoginBackMsg back = action.loginAction("1", "1");
        System.out.println("back = " + back);
    }

    /**
     * 测试登录
     */
    @Test
    public void testLogin2() {
        LoginBackMsg back = WebOperator.login("2", "2");
        System.out.println("s = " + back);
    }


    /**
     * 测试获取用户信息
     */
    @Test
    public void testGetById() {
        UserActionClient action = Feign.builder()
                .decoder(new GsonDecoder())
                .target(UserActionClient.class, "http://localhost:8080/");
        UserDTO s = action.getById(2);
        System.out.println("s = " + s);
    }
}
