package com.feng.common.entity;

import java.util.Date;

import lombok.Data;

@Data
public class User {
    private Integer userId;
    private String userName;
    private String nickName;
    private Date registerTime;
}