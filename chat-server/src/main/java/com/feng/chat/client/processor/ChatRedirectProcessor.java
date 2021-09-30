package com.feng.chat.client.processor;

import com.feng.chat.client.session.LocalSession;
import com.feng.chat.client.session.ServerSession;
import com.feng.chat.client.session.SessionManager;
import com.feng.common.msg.ProtoMsg;
import com.feng.common.util.Logger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description TODO
 * @Author fengsy
 * @Date 9/30/21
 */
@Slf4j
@Service("ChatRedirectProcessor")
public class ChatRedirectProcessor extends AbstractServerProcessor {

    @Override
    public ProtoMsg.HeadType op() {
        return ProtoMsg.HeadType.MESSAGE_REQUEST;
    }

    @Override
    public boolean action(LocalSession ch, ProtoMsg.Message proto) {
        // 聊天处理
        ProtoMsg.MessageRequest messageRequest = proto.getMessageRequest();
        Logger.tcfo("chatMsg | from="
                + messageRequest.getFrom()
                + " , to =" + messageRequest.getTo()
                + " , MsgType =" + messageRequest.getMsgType()
                + " , content =" + messageRequest.getContent());

        // 获取接收方的chatID
        String to = messageRequest.getTo();
        // int platform = messageRequest.getPlatform();
        List<ServerSession> toSessions = SessionManager.getSingletonInstance().getSessionsBy(to);
        if (toSessions == null) {
            //接收方离线
            Logger.tcfo("[" + to + "] 不在线，需要保存为离线消息，请保存到nosql如mongo中!");
        } else {

            toSessions.forEach((session) ->
            {
                // 将IM消息发送到接收客户端；
                // 如果是remotesession，则转发到对应的服务节点
                session.writeAndFlush(proto);

            });
        }
        return true;
    }
}
