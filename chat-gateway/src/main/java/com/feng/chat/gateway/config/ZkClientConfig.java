package com.feng.chat.gateway.config;

import com.feng.chat.common.util.SpringContextUtil;
import com.feng.chat.common.zk.CuratorZKclient;
import com.feng.chat.gateway.balance.ImLoadBalance;
import com.feng.chat.gateway.balance.LoadBalance;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    @Bean(name = "imLoadBalance")
    public LoadBalance imLoadBalance(CuratorZKclient curatorZKClient) {

        return new ImLoadBalance(curatorZKClient);
    }

}