package com.feng.common.zk;

import lombok.Data;
import org.apache.curator.framework.CuratorFramework;

/**
 * @Description 使用curator framework 实现的zk client
 * @Author fengsy
 * @Date 9/29/21
 */
@Data
public class CuratorZKclient {

    private final String sessionTimeoutMs;
    private CuratorFramework client;

    private String address = "127.0.0.1:2181";

    public static CuratorZKclient instance = null;

    public CuratorZKclient(String address, String sessionTimeoutMs) {
        this.address = address;
        this.sessionTimeoutMs = sessionTimeoutMs;
        init();
    }

    private void init() {
        if (client != null) {
            return;
        }
        client = CuratorZkClientFactory.createSimple(address, sessionTimeoutMs);
        client.start();
        instance = this;
    }

}
