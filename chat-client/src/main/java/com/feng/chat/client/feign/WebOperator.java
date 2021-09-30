package com.feng.chat.client.feign;

import com.feng.common.constants.ServerConstants;
import com.feng.common.entity.LoginBackMsg;
import com.feng.common.util.JsonUtil;
import feign.Feign;
import feign.codec.StringDecoder;

public class WebOperator {

    public static LoginBackMsg login(String userName, String password) {
        UserActionClient action = Feign.builder()
                .decoder(new StringDecoder())
                .target(UserActionClient.class, ServerConstants.WEB_URL);

        String s = action.loginAction(userName, password);

        LoginBackMsg backMsg = JsonUtil.jsonToPojo(s, LoginBackMsg.class);
        return backMsg;
    }
}
