package com.feng.chat.client.feign;

import com.feng.chat.common.constants.ServerConstants;
import com.feng.chat.common.entity.LoginBackMsg;
import feign.Feign;
import feign.gson.GsonDecoder;

public class WebOperator {

    public static LoginBackMsg login(String userName, String password) {
        UserActionClient action = Feign.builder()
                .decoder(new GsonDecoder())
                .target(UserActionClient.class, ServerConstants.WEB_URL);

        LoginBackMsg backMsg = action.loginAction(userName, password);

        return backMsg;
    }
}
