package com.feng.chat.web.redis;

import com.alibaba.fastjson.JSONObject;
import com.feng.chat.web.handler.WebsocketRouterHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

/**
 * @Description TODO
 * @Author fengsy
 * @Date 11/30/21
 */
@Component
@Slf4j
public class NewMessageListener implements MessageListener {

    @Autowired
    private WebsocketRouterHandler websocketRouterHandler;

    StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
    private static final RedisSerializer<String> valueSerializer = new GenericToStringSerializer(Object.class);

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String topic = stringRedisSerializer.deserialize(message.getChannel());
        String jsonMsg = valueSerializer.deserialize(message.getBody());
        log.info("Message Received --> pattern: {}，topic:{}，message: {}", new String(pattern), topic, jsonMsg);
        JSONObject msgJson = JSONObject.parseObject(jsonMsg);
        long otherUid = msgJson.getLong("otherUid");
        JSONObject pushJson = new JSONObject();
        pushJson.put("type", 4);
        pushJson.put("data", msgJson);

        websocketRouterHandler.pushMsg(otherUid, pushJson);

    }
}
