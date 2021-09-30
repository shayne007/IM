package com.feng.common.concurrent;

import com.feng.common.util.ThreadUtil;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Description 异步定时任务调度器
 * @Author fengsy
 * @Date 9/29/21
 */
public class FutureTaskScheduler {
    private static ThreadPoolExecutor mixPool = null;

    static {
        mixPool = ThreadUtil.getMixedTargetThreadPool();
    }

    private FutureTaskScheduler() {
    }

    public static void add(Runnable executeTask) {
        mixPool.submit(() -> {
            executeTask.run();
        });
    }

}
