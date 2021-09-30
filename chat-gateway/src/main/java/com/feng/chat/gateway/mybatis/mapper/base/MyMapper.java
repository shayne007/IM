package com.feng.chat.gateway.mybatis.mapper.base;

import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

/**
 * @Description base Mapper
 * @Author fengsy
 * @Date 9/29/21
 */
public interface MyMapper<T> extends Mapper<T>, MySqlMapper<T> {

}
