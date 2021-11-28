package com.feng.chat.server.distribute;

import com.feng.chat.common.constants.ServerConstants;
import com.feng.chat.common.zk.CuratorZKclient;
import lombok.Getter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.retry.RetryNTimes;

/**
 * @Description 基于ZK的分布式计数器, 用于统计在线用户数(底层实现：竞争大时才使用zk分布式计数)
 * @Author fengsy
 * @Date 9/29/21
 */
public class OnlineCounter {
    private static final String PATH = ServerConstants.COUNTER_PATH;

    //Zk客户端
    private CuratorFramework client = null;

    //单例模式
    private static OnlineCounter singleInstance = null;

    DistributedAtomicLong distributedAtomicLong = null;

    @Getter
    private Long curValue;

    public static OnlineCounter getInst() {
        if (null == singleInstance) {
            singleInstance = new OnlineCounter();
            singleInstance.client = CuratorZKclient.instance.getClient();
            singleInstance.init();
        }
        return singleInstance;
    }

    private void init() {
        //分布式计数器，失败时重试10，每次间隔30毫秒
        distributedAtomicLong =
                new DistributedAtomicLong(client, PATH, new RetryNTimes(10, 30));
    }

    private OnlineCounter() {

    }

    /**
     * 增加计数
     */
    public boolean increment() {
        boolean result = false;
        AtomicValue<Long> val = null;
        try {
            val = distributedAtomicLong.increment();
            result = val.succeeded();
            System.out.println("old cnt: " + val.preValue()
                    + "   new cnt : " + val.postValue()
                    + "  result:" + val.succeeded());
            curValue = val.postValue();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 减少计数
     */
    public boolean decrement() {
        boolean result = false;
        AtomicValue<Long> val = null;
        try {
            val = distributedAtomicLong.decrement();
            result = val.succeeded();
            System.out.println("old cnt: " + val.preValue()
                    + "   new cnt : " + val.postValue()
                    + "  result:" + val.succeeded());
            curValue = val.postValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;

    }
}
