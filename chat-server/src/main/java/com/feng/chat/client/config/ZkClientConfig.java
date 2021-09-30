package com.feng.chat.client.config;

import com.feng.common.util.SpringContextUtil;
import com.feng.common.zk.CuratorZKclient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description curator zk client 配置类
 * @Author fengsy
 * @Date 9/29/21
 */
@Configuration
public class ZkClientConfig implements ApplicationContextAware {

    @Value("${zookeeper.connect.url}")
    private String zkConnect;

    @Value("${zookeeper.connect.SessionTimeout}")
    private String zkSessionTimeout;

    /**
     * @see BeanInitializationException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        SpringContextUtil.setContext(applicationContext);

    }

    @Bean(name = "curatorZKClient")
    public CuratorZKclient curatorZKClient() {

        return new CuratorZKclient(zkConnect, zkSessionTimeout);
    }

}