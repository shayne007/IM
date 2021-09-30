package com.feng.chat.client.processor;


import com.feng.common.msg.ProtoMsg;

import java.util.HashMap;
import java.util.Map;

public class ProcessorFactory {

    private static ProcessorFactory instance;

    public static Map<ProtoMsg.HeadType, ServerReciever> factory
            = new HashMap<ProtoMsg.HeadType, ServerReciever>();

    static {
        instance = new ProcessorFactory();
    }

    private ProcessorFactory() {
        try {

            ServerReciever proc = new LoginProcessor();
            factory.put(proc.op(), proc);

            proc = new ChatRedirectProcessor();
            factory.put(proc.op(), proc);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static ProcessorFactory getInstance() {
        return instance;
    }

    public ServerReciever getOperation(ProtoMsg.HeadType type) {
        return factory.get(type);
    }


}
