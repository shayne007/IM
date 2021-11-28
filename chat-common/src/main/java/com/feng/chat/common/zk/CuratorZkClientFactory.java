package com.feng.chat.common.zk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * @Description zookeeper client 工厂类创建CuratorFramework实例
 * @Author fengsy
 * @Date 9/29/21
 */
public class CuratorZkClientFactory {
    /**
     * @param connectStr zk的连接地址
     * @param timeoutMs  超时时间
     * @return CuratorFramework实例
     */
    public static CuratorFramework createSimple(String connectStr, String timeoutMs) {
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
        return CuratorFrameworkFactory.builder().connectString(connectStr)
                .connectionTimeoutMs(Integer.parseInt(timeoutMs)).sessionTimeoutMs(Integer.parseInt(timeoutMs))
                .retryPolicy(retryPolicy).build();
    }

    /**
     * @param connectionStr       zk的连接地址
     * @param retryPolicy         重试策略
     * @param connectionTimeoutMs
     * @param sessionTimeoutMs
     * @return CuratorFramework实例
     */
    public static CuratorFramework createWithOptions(String connectionStr, RetryPolicy retryPolicy,
                                                     int connectionTimeoutMs, int sessionTimeoutMs) {

        // builder 模式创建 CuratorFramework 实例
        return CuratorFrameworkFactory.builder().connectString(connectionStr).retryPolicy(retryPolicy)
                .connectionTimeoutMs(connectionTimeoutMs).sessionTimeoutMs(sessionTimeoutMs)
                // 其他的创建选项
                .build();
    }
}