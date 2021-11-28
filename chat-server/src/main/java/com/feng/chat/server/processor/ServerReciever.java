package com.feng.chat.server.processor;


import com.feng.chat.common.msg.proto.ProtoMsg;
import com.feng.chat.server.session.LocalSession;

/**
 * 操作类
 */
public interface ServerReciever {

    ProtoMsg.HeadType msgType();

    boolean action(LocalSession session, ProtoMsg.Message proto);

}
