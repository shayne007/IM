package com.feng.chat.common.msg;

import com.feng.chat.common.msg.proto.ProtoMsg;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

@Data
public class ChatMsg {

    // 消息类型 1：纯文本 2：音频 3：视频 4：地理位置 5：其他
    public enum MSGTYPE {
        TEXT, AUDIO, VIDEO, POS, OTHER;
    }

    public ChatMsg(UserDTO user) {
        if (null == user) {
            return;
        }
        this.user = user;
        this.setTime(System.currentTimeMillis());
        this.setFrom(user.getUserId());
        this.setFromNick(user.getNickName());

    }

    private UserDTO user;

    private long msgId;
    private String from;
    private String to;
    private long time;
    private MSGTYPE msgType;
    private String content;
    private String url; // 多媒体地址
    private String property; // 附加属性
    private String fromNick; // 发送者昵称
    private String json; // 附加的json串

    public void fillMsg(ProtoMsg.MessageRequest.Builder builder) {
        if (msgId > 0) {
            builder.setMsgId(msgId);
        }
        if (StringUtils.isNotEmpty(from)) {
            builder.setFrom(from);
        }
        if (StringUtils.isNotEmpty(to)) {
            builder.setTo(to);
        }
        if (time > 0) {
            builder.setTime(time);
        }
        if (msgType != null) {
            builder.setMsgType(msgType.ordinal());
        }
        if (StringUtils.isNotEmpty(content)) {
            builder.setContent(content);
        }
        if (StringUtils.isNotEmpty(url)) {
            builder.setUrl(url);
        }
        if (StringUtils.isNotEmpty(property)) {
            builder.setProperty(property);
        }
        if (StringUtils.isNotEmpty(fromNick)) {
            builder.setFromNick(fromNick);
        }

        if (StringUtils.isNotEmpty(json)) {
            builder.setJson(json);
        }
    }

}
