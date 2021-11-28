package com.feng.chat.server.protoBuilder;


import com.feng.chat.common.constants.ResultCodeEnum;
import com.feng.chat.common.msg.proto.ProtoMsg;

public class ChatMsgBuilder {

    public static ProtoMsg.Message buildChatResponse(long seqId, ResultCodeEnum en) {
        // 设置消息类型
        ProtoMsg.Message.Builder mb = ProtoMsg.Message.newBuilder().setType(ProtoMsg.HeadType.MESSAGE_RESPONSE)
                .setSequence(seqId); // 设置应答流水，与请求对应
        ProtoMsg.MessageResponse.Builder rb =
                ProtoMsg.MessageResponse.newBuilder().setCode(en.getCode()).setInfo(en.getDesc()).setExpose(1);
        mb.setMessageResponse(rb.build());
        return mb.build();
    }

    /**
     * 登录应答 应答消息protobuf
     */
    public static ProtoMsg.Message buildLoginResponce(ResultCodeEnum en, long seqId) {
        // 设置消息类型
        ProtoMsg.Message.Builder mb = ProtoMsg.Message.newBuilder().setType(ProtoMsg.HeadType.MESSAGE_RESPONSE)
                .setSequence(seqId); // 设置应答流水，与请求对应

        ProtoMsg.LoginResponse.Builder rb =
                ProtoMsg.LoginResponse.newBuilder().setCode(en.getCode()).setInfo(en.getDesc()).setExpose(1);

        mb.setLoginResponse(rb.build());
        return mb.build();
    }

}
