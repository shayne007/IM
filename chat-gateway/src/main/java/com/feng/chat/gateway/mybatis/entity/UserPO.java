package com.feng.chat.gateway.mybatis.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description User持久化对象
 * @Author fengsy
 * @Date 9/29/21
 */
@Data
@NoArgsConstructor
@Table(name = "t_user")
public class UserPO implements Serializable {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String userId;
    @Column(name = "user_name")
    private String userName;
    @Column(name = "nick_name")
    private String nickName;
    @Column(name = "register_time")
    private Date registerTime;
    @Column(name = "password")
    private String password;

}
