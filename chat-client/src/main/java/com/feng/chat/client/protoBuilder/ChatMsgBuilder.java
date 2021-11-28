/**
 * Created by 尼恩 at 疯狂创客圈
 */

package com.feng.chat.client.protoBuilder;


import com.feng.chat.client.session.ClientSession;
import com.feng.chat.common.msg.ChatMsg;
import com.feng.chat.common.msg.UserDTO;
import com.feng.chat.common.msg.proto.ProtoMsg;

/**
 * 聊天消息Builder
 */

public class ChatMsgBuilder extends BaseBuilder {


    private ChatMsg chatMsg;
    private UserDTO user;


    public ChatMsgBuilder(ChatMsg chatMsg, UserDTO user, ClientSession session) {
        super(ProtoMsg.HeadType.MESSAGE_REQUEST, session);
        this.chatMsg = chatMsg;
        this.user = user;

    }


    public ProtoMsg.Message build() {
        ProtoMsg.Message message = buildCommon(-1);
        ProtoMsg.MessageRequest.Builder cb
                = ProtoMsg.MessageRequest.newBuilder();

        chatMsg.fillMsg(cb);
        return message
                .toBuilder()
                .setMessageRequest(cb)
                .build();
    }

    public static ProtoMsg.Message buildChatMsg(
            ChatMsg chatMsg,
            UserDTO user,
            ClientSession session) {
        ChatMsgBuilder builder =
                new ChatMsgBuilder(chatMsg, user, session);
        return builder.build();

    }
}