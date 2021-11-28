package com.feng.chat.server.processor;

import com.feng.chat.common.constants.ResultCodeEnum;
import com.feng.chat.common.msg.UserDTO;
import com.feng.chat.common.msg.proto.ProtoMsg;
import com.feng.chat.server.feign.UserActionClient;
import com.feng.chat.server.protoBuilder.LoginResponseBuilder;
import com.feng.chat.server.session.LocalSession;
import com.feng.chat.server.session.ServerSession;
import com.feng.chat.server.session.SessionManager;
import feign.Feign;
import feign.gson.GsonDecoder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @Description 登录请求 处理逻辑，IO线程与业务线程相互隔离: 使用线程池异步处理业务逻辑
 * @Author fengsy
 * @Date 9/30/21
 */
@Data
@Slf4j
@Service("LoginProcessor")
public class LoginProcessor extends AbstractServerProcessor {
    @Autowired
    LoginResponseBuilder loginResponseBuilder;
    @Autowired
    SessionManager sessionManager;

    @Override
    public ProtoMsg.HeadType msgType() {
        return ProtoMsg.HeadType.LOGIN_REQUEST;
    }

    @Override
    public boolean action(LocalSession session, ProtoMsg.Message proto) {
        // 取出token验证
        ProtoMsg.LoginRequest info = proto.getLoginRequest();
        long seqNo = proto.getSequence();

        UserDTO user = UserDTO.fromMsg(info);

        //检查用户
        boolean isValidUser = checkUser(user);
        if (!isValidUser) {
            ResultCodeEnum resultcode = ResultCodeEnum.NO_TOKEN;
            ProtoMsg.Message response = loginResponseBuilder.loginResponse(resultcode, seqNo, "-1");
            //发送之后，断开连接
            session.writeAndClose(response);
            return false;
        }

        session.setUser(user);

        // 绑定session
        session.bind();
        sessionManager.addLocalSession(session);

        ResultCodeEnum resultcode = ResultCodeEnum.SUCCESS;
        ProtoMsg.Message response = loginResponseBuilder.loginResponse(resultcode, seqNo, session.getSessionId());
        session.writeAndFlush(response);
        return true;
    }

    private boolean checkUser(UserDTO user) {
        //校验用户,比较耗时的操作,需要100 ms以上的时间
        //方法1：调用远程用户restfull 校验服务
        //方法2：调用数据库接口校验
        UserActionClient action = Feign.builder()
                .decoder(new GsonDecoder())
                .target(UserActionClient.class, "http://localhost:8080/");
        UserDTO userDTO = action.getById(user.getUserId());
        Objects.requireNonNull(userDTO);

        //检查用户登录session是否已存在
        List<ServerSession> sessionList = sessionManager.getSessionsBy(user.getUserId());
        if (null != sessionList && sessionList.size() > 0) {
            return false;
        }
        return true;
    }
}
