package com.feng.chat.gateway;


import com.feng.chat.common.entity.ImNode;
import com.feng.chat.common.util.ThreadUtil;
import com.feng.chat.gateway.balance.ImLoadBalance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ChatGatewayApplication.class)
public class TestLoadBalanceService {

    @Autowired
    private ImLoadBalance imLoadBalance;

    @Test
    public void testGetBestWorker() throws Exception {

        ImNode bestWorker = imLoadBalance.getBestWorker();

        System.out.println("bestWorker = " + bestWorker);

        ThreadUtil.sleepSeconds(Integer.MAX_VALUE);
    }


}
