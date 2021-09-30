package com.feng.chat.gateway.controller;

import com.feng.chat.gateway.balance.LoadBalance;
import com.feng.chat.gateway.mybatis.entity.UserPO;
import com.feng.chat.gateway.service.UserService;
import com.feng.common.entity.ImNode;
import com.feng.common.entity.LoginBackMsg;
import com.feng.common.msg.UserDTO;
import com.feng.common.util.JsonUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @Description 用户控制类
 * @Author fengsy
 * @Date 9/29/21
 */
@RestController
@RequestMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    @Resource
    private LoadBalance inLoadBalance;
    @Resource
    private UserService userService;

    @GetMapping("/login/{username}/{pwd}")
    public String userLogin(@PathVariable("username") String username, @PathVariable("pwd") String pwd) {
        UserPO user = new UserPO();
        user.setUserName(username);
        user.setPassword(pwd);
        UserPO loginUser = userService.login(user);

        Objects.requireNonNull(loginUser);

        LoginBackMsg msg = new LoginBackMsg();
        List<ImNode> workers = inLoadBalance.getWorkers();
        msg.setImNodeList(workers);

        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(loginUser, userDTO);
        msg.setUserDTO(userDTO);

        msg.setToken(user.getUserId());
        String result = JsonUtil.pojoToJson(msg);
        return result;
    }
}
