package com.feng.chat.common.msg;

import com.feng.chat.common.msg.proto.ProtoMsg;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description 用户类的数据传输对象
 * @Author fengsy
 * @Date 9/29/21
 */
@Slf4j
@Data
public class UserDTO {
    String userId;
    String userName;
    String devId;
    String token;
    String nickName = "nickName";
    PLATTYPE platform = PLATTYPE.MAC;

    public enum PLATTYPE {
        WINDOWS, MAC, ANDROID, IOS, WEB, OTHER;
    }

    private String sessionId;

    public void setPlatform(int platform) {
        PLATTYPE[] values = PLATTYPE.values();
        for (int i = 0; i < values.length; i++) {
            if (values[i].ordinal() == platform) {
                this.platform = values[i];
            }
        }

    }

    public static UserDTO fromMsg(ProtoMsg.LoginRequest info) {
        UserDTO user = new UserDTO();
        user.userId = info.getUid();
        user.devId = info.getDeviceId();
        user.token = info.getToken();
        user.setPlatform(info.getPlatform());
        log.info("登录中: {}", user.toString());
        return user;

    }
}
