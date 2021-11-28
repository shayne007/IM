package com.feng.chat.server.processor;


import com.feng.chat.common.msg.proto.ProtoMsg;
import com.feng.chat.server.session.LocalSession;

/**
 * 操作类
 */
public interface ServerProcessor {

    ProtoMsg.HeadType type();

    boolean action(LocalSession ch, ProtoMsg.Message proto);

}
