package com.feng.chat.client.processor;

import com.feng.chat.client.protoBuilder.LoginResponseBuilder;
import com.feng.chat.client.session.LocalSession;
import com.feng.chat.client.session.SessionManager;
import com.feng.common.constants.ResultCodeEnum;
import com.feng.common.msg.ProtoMsg;
import com.feng.common.msg.UserDTO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description TODO
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
    public ProtoMsg.HeadType op() {
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
            ProtoMsg.Message response = loginResponseBuilder.loginResponce(resultcode, seqNo, "-1");
            //发送之后，断开连接
            session.writeAndClose(response);
            return false;
        }

        session.setUser(user);

        /**
         * 绑定session
         */
        session.bind();
        sessionManager.addLocalSession(session);


        /**
         * 通知客户端：登录成功
         */

        ResultCodeEnum resultcode = ResultCodeEnum.SUCCESS;
        ProtoMsg.Message response =
                loginResponseBuilder.loginResponce(resultcode, seqNo, session.getSessionId());
        session.writeAndFlush(response);
        return true;
    }

    private boolean checkUser(UserDTO user) {
        //校验用户,比较耗时的操作,需要100 ms以上的时间
        //方法1：调用远程用户restfull 校验服务
        //方法2：调用数据库接口校验

//        List<ServerSession> l = sessionManger.getSessionsBy(user.getUserId());
//
//
//        if (null != l && l.size() > 0)
//        {
//            return false;
//        }

        return true;
    }
}
