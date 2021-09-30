package com.feng.chat.gateway.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.feng.chat.gateway.mybatis.entity.UserPO;
import com.feng.chat.gateway.mybatis.mapper.UserMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * @Description user service implementation
 * @Author fengsy
 * @Date 9/29/21
 */
@Service
@Slf4j
public class UserService {
    @Resource
    UserMapper userMapper;

    public UserPO login(UserPO user) {
        UserPO sample = new UserPO();
        sample.setUserName(user.getUserName());
        UserPO u = userMapper.selectOne(sample);
        if (null == u) {
            log.info("找不到用户信息 username={}", user.getUserName());
            return null;

        }
        return u;
    }

}
