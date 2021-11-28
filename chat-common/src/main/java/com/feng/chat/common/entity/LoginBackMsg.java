package com.feng.chat.common.entity;

import com.feng.chat.common.msg.UserDTO;
import lombok.Data;

import java.util.List;

/**
 * @Description 用户登录成功返回的信息
 * @Author fengsy
 * @Date 9/29/21
 */
@Data
public class LoginBackMsg {
    List<ImNode> imNodeList;

    private String token;

    private UserDTO userDTO;
}
