package com.feng.chat.client.feign;


import com.feng.chat.common.entity.LoginBackMsg;
import com.feng.chat.common.msg.UserDTO;
import feign.Param;
import feign.RequestLine;

/**
 * 远程接口的本地代理
 */
public interface UserActionClient {

    /**
     * 登录代理
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    @RequestLine("GET /user/login/{username}/{password}")
    LoginBackMsg loginAction(
            @Param("username") String username,
            @Param("password") String password);


    /**
     * 获取用户信息代理
     *
     * @param userid 用户id
     * @return 用户信息
     */
    @RequestLine("GET /user/{userid}")
    UserDTO getById(@Param("userid") Integer userid);


}
