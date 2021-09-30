package com.feng.common.entity;

import java.util.List;

import com.feng.common.msg.UserDTO;

import lombok.Data;

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
