package com.feng.chat.client.processor;


import com.feng.chat.client.session.LocalSession;
import com.feng.common.msg.ProtoMsg;

/**
 * 操作类
 */
public interface ServerReciever {

    ProtoMsg.HeadType op();

    boolean action(LocalSession session, ProtoMsg.Message proto);

}
