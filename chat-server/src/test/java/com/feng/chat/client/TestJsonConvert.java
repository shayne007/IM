package com.feng.chat.client;

import com.alibaba.fastjson.TypeReference;
import com.feng.chat.common.entity.ImNode;
import com.feng.chat.common.msg.Notification;
import com.feng.chat.common.util.FastJsonUtil;
import com.feng.chat.common.util.GsonUtil;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;

/**
 * @Description gson转换 测试用例
 * @Author fengsy
 * @Date 10/3/21
 */
public class TestJsonConvert {
    @Test
    public void testNotificationJsonConvert() {

        ImNode imNode = new ImNode("unKnown", 111);

        Notification<ImNode> notification = new Notification<>(imNode);
        notification.setType(Notification.CONNECT_FINISHED);
        String json = GsonUtil.pojoToJson(notification);

        Notification<ImNode> no2 = GsonUtil.
                jsonToPojo(json, new TypeToken<Notification<ImNode>>() {
                }.getType());

        System.out.println("no2 = " + no2);
        System.out.println("im node data type = " + no2.getData().getClass().getName());

    }

    @Test
    public void testJsonConvertSpeed() {
        ImNode imNode = new ImNode("unKnown", 111);

        Notification<ImNode> notification = new Notification<>(imNode);
        notification.setType(Notification.CONNECT_FINISHED);
        long start = System.currentTimeMillis();
        String json1 = GsonUtil.pojoToJson(notification);
        long end = System.currentTimeMillis();
        System.out.println("Gson parse object to json string cost time: " + (end - start));
        start = System.currentTimeMillis();
        String json2 = FastJsonUtil.pojoToJson(notification);
        end = System.currentTimeMillis();
        System.out.println("fastjson parse object to json string cost time: " + (end - start));

        start = System.currentTimeMillis();
        Notification<ImNode> no1 = GsonUtil.
                jsonToPojo(json1, new TypeToken<Notification<ImNode>>() {
                }.getType());
        end = System.currentTimeMillis();
        System.out.println("gson parse json string to object cost time: " + (end - start));
        System.out.println(no1);

        start = System.currentTimeMillis();
        Notification<ImNode> no2 = FastJsonUtil.jsonToPojo(json2, new TypeReference<Notification<ImNode>>() {
        }.getType());
        end = System.currentTimeMillis();
        System.out.println("fastjson parse json string to  object cost time: " + (end - start));
        System.out.println(no2);

    }

}
