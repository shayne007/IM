package com.feng.chat.client.processor;


import com.feng.chat.client.session.LocalSession;
import com.feng.common.msg.ProtoMsg;

/**
 * 操作类
 */
public interface ServerProcessor
{

    ProtoMsg.HeadType type();

    boolean action(LocalSession ch, ProtoMsg.Message proto);

}
