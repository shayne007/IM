package com.feng.chat.common.entity;

import lombok.Data;

import java.util.Date;

@Data
public class User {
    private Integer userId;
    private String userName;
    private String nickName;
    private Date registerTime;
}