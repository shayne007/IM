package com.feng.chat.client;

import com.feng.chat.common.util.GsonUtil;
import com.feng.chat.common.util.Logger;
import com.feng.chat.common.util.ThreadUtil;
import com.feng.chat.server.ChatServerApplication;
import com.feng.chat.server.distribute.ImWorker;
import com.feng.chat.server.distribute.WorkerRouter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

/**
 * @Description zk service单元测试
 * @Author fengsy
 * @Date 10/1/21
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ChatServerApplication.class)
public class TestZKService {
    @Test
    public void testNodeName() throws Exception {
        Class clazz = ImWorker.class;
        Constructor<?> constructor = clazz.getDeclaredConstructor();//获得私有构造方法
        constructor.setAccessible(true);
        ImWorker worker = (ImWorker) constructor.newInstance();
        worker.init();
        Logger.cfo("worker = " + GsonUtil.pojoToJson(worker.getLocalNode()));
        ThreadUtil.sleepSeconds(Integer.MAX_VALUE);
    }

    @Test
    public void testWorkerFound() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class clazz = WorkerRouter.class;
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        WorkerRouter workerRouter = (WorkerRouter) constructor.newInstance();
        workerRouter.setRunAfterAdd(null);
        workerRouter.setRunAfterRemove(null);
        workerRouter.init();

        ThreadUtil.getIoIntenseTargetThreadPool().submit(() -> {
            try {
                Class cls1 = ImWorker.class;
                Constructor<?> cons1 = cls1.getDeclaredConstructor();//获得私有构造方法
                cons1.setAccessible(true);
                ImWorker worker = (ImWorker) cons1.newInstance();
                worker.init();
                Logger.cfo("worker = " + GsonUtil.pojoToJson(worker.getLocalNode()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        ThreadUtil.sleepSeconds(Integer.MAX_VALUE);

    }

    @Test
    public void testIncBalance() throws Exception {

        Class clazz = ImWorker.class;
        Constructor<?> cons = clazz.getDeclaredConstructor();//获得私有构造方法
        cons.setAccessible(true);
        ImWorker worker = (ImWorker) cons.newInstance();

        worker.init();
        Logger.cfo("worker = " + GsonUtil.pojoToJson(worker.getLocalNode()));

        ThreadUtil.scheduleAtFixedRate(() -> {
            worker.increLoad();
            worker.getPathRegistered();

        }, 1, TimeUnit.SECONDS);

        ThreadUtil.sleepSeconds(Integer.MAX_VALUE);
    }
}
