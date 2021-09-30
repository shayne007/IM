package com.feng.chat.gateway.mybatis.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.feng.chat.gateway.mybatis.entity.UserPO;
import com.feng.chat.gateway.mybatis.mapper.base.MyMapper;

/**
 * @author fengsy
 * @Description
 * @date 9/29/21
 */
@Mapper
public interface UserMapper extends MyMapper<UserPO> {

}
