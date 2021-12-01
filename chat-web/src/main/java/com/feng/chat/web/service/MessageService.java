package com.feng.chat.web.service;

import com.alibaba.fastjson.JSONObject;
import com.feng.chat.web.Constants;
import com.feng.chat.web.dao.MessageContactRepository;
import com.feng.chat.web.dao.MessageContentRepository;
import com.feng.chat.web.dao.MessageRelationRepository;
import com.feng.chat.web.dao.UserRepository;
import com.feng.chat.web.entity.ContactMultiKeys;
import com.feng.chat.web.entity.MessageContact;
import com.feng.chat.web.entity.MessageContent;
import com.feng.chat.web.entity.MessageRelation;
import com.feng.chat.web.entity.User;
import com.feng.chat.web.vo.MessageContactVO;
import com.feng.chat.web.vo.MessageVO;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class MessageService {
    @Autowired
    private MessageContentRepository contentRepository;
    @Autowired
    private MessageRelationRepository relationRepository;
    @Autowired
    private MessageContactRepository contactRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RedisTemplate redisTemplate;

    public MessageVO sendNewMsg(long senderUid, long recipientUid, String content, int msgType) {
        Date currentTime = new Date();
        /**存内容*/
        MessageContent messageContent = new MessageContent();
        messageContent.setSenderId(senderUid);
        messageContent.setRecipientId(recipientUid);
        messageContent.setContent(content);
        messageContent.setMsgType(msgType);
        messageContent.setCreateTime(currentTime);
        messageContent = contentRepository.saveAndFlush(messageContent);
        Long mid = messageContent.getMid();

        /**存发件人的发件箱*/
        MessageRelation messageRelationSender = new MessageRelation();
        messageRelationSender.setMid(mid);
        messageRelationSender.setOwnerUid(senderUid);
        messageRelationSender.setOtherUid(recipientUid);
        messageRelationSender.setType(0);
        messageRelationSender.setCreateTime(currentTime);
        relationRepository.save(messageRelationSender);

        /**存收件人的收件箱*/
        MessageRelation messageRelationRecipient = new MessageRelation();
        messageRelationRecipient.setMid(mid);
        messageRelationRecipient.setOwnerUid(recipientUid);
        messageRelationRecipient.setOtherUid(senderUid);
        messageRelationRecipient.setType(1);
        messageRelationRecipient.setCreateTime(currentTime);
        relationRepository.save(messageRelationRecipient);

        /**更新发件人的最近联系人 */
        MessageContact messageContactSender = contactRepository.findById(new ContactMultiKeys(senderUid,
                recipientUid)).orElse(null);
        if (messageContactSender != null) {
            messageContactSender.setMid(mid);
        } else {
            messageContactSender = new MessageContact();
            messageContactSender.setOwnerUid(senderUid);
            messageContactSender.setOtherUid(recipientUid);
            messageContactSender.setMid(mid);
            messageContactSender.setCreateTime(currentTime);
            messageContactSender.setType(0);
        }
        contactRepository.save(messageContactSender);

        /**更新收件人的最近联系人 */
        MessageContact messageContactRecipient = contactRepository.findById(new ContactMultiKeys(recipientUid,
                senderUid)).orElse(null);
        if (messageContactRecipient != null) {
            messageContactRecipient.setMid(mid);
        } else {
            messageContactRecipient = new MessageContact();
            messageContactRecipient.setOwnerUid(recipientUid);
            messageContactRecipient.setOtherUid(senderUid);
            messageContactRecipient.setMid(mid);
            messageContactRecipient.setCreateTime(currentTime);
            messageContactRecipient.setType(1);
        }
        contactRepository.save(messageContactRecipient);

        /**更未读更新 */
        SessionCallback<Boolean> sessionCallback = new SessionCallback<Boolean>() {
            List<Object> exec = null;

            @Override
            @SuppressWarnings("unchecked")
            public Boolean execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                redisTemplate.opsForValue().increment(recipientUid + Constants.TOTAL_UNREAD_SUFFIX, 1); //加总未读
                redisTemplate.opsForHash().increment(recipientUid + Constants.CONVERSION_UNREAD_SUFFIX, senderUid, 1); //加会话未读
                exec = operations.exec();
                if (exec.size() > 0) {
                    return true;
                }
                return false;
            }
        };
        redisTemplate.execute(sessionCallback);

        /** 待推送消息发布到redis */
        User self = userRepository.findById(senderUid).get();
        User other = userRepository.findById(recipientUid).get();
        MessageVO messageVO = new MessageVO(mid, content, self.getUid(), messageContactSender.getType(), other.getUid(), messageContent.getCreateTime(), self.getAvatar(), other.getAvatar(), self.getUsername(), other.getUsername());
        redisTemplate.convertAndSend(Constants.WEBSOCKET_MSG_TOPIC, JSONObject.toJSONString(messageVO));

        return messageVO;
    }

    public List<MessageVO> queryConversationMsg(long ownerUid, long otherUid) {
        List<MessageRelation> relationList = relationRepository.findAllByOwnerUidAndOtherUidOrderByMidAsc(ownerUid, otherUid);
        return composeMessageVO(relationList, ownerUid, otherUid);
    }

    public List<MessageVO> queryNewerMsgFrom(long ownerUid, long otherUid, long fromMid) {
        List<MessageRelation> relationList = relationRepository.findAllByOwnerUidAndOtherUidAndMidIsGreaterThanOrderByMidAsc(ownerUid, otherUid, fromMid);
        return composeMessageVO(relationList, ownerUid, otherUid);
    }

    private List<MessageVO> composeMessageVO(List<MessageRelation> relationList, long ownerUid, long otherUid) {
        if (null != relationList && !relationList.isEmpty()) {
            /** 先拼接消息索引和内容 */
            List<MessageVO> msgList = Lists.newArrayList();
            User self = userRepository.findById(ownerUid).get();
            User other = userRepository.findById(otherUid).get();
            relationList.stream().forEach(relation -> {
                Long mid = relation.getMid();
                MessageContent contentVO = contentRepository.findById(mid).get();
                if (null != contentVO) {
                    String content = contentVO.getContent();
                    MessageVO messageVO = new MessageVO(mid, content, relation.getOwnerUid(), relation.getType(), relation.getOtherUid(), relation.getCreateTime(), self.getAvatar(), other.getAvatar(), self.getUsername(), other.getUsername());
                    msgList.add(messageVO);
                }
            });

            /** 再变更未读 */
            Object convUnreadObj = redisTemplate.opsForHash().get(ownerUid + Constants.CONVERSION_UNREAD_SUFFIX, otherUid);
            if (null != convUnreadObj) {
                long convUnread = Long.parseLong((String) convUnreadObj);
                redisTemplate.opsForHash().delete(ownerUid + Constants.CONVERSION_UNREAD_SUFFIX, otherUid);
                long afterCleanUnread = redisTemplate.opsForValue().increment(ownerUid + Constants.TOTAL_UNREAD_SUFFIX, -convUnread);
                /** 修正总未读 */
                if (afterCleanUnread <= 0) {
                    redisTemplate.delete(ownerUid + Constants.TOTAL_UNREAD_SUFFIX);
                }
            }
            return msgList;
        }
        return null;
    }

    public MessageContactVO queryContacts(long ownerUid) {
        List<MessageContact> contacts = contactRepository.findMessageContactsByOwnerUidOrderByMidDesc(ownerUid);
        if (contacts != null) {
            User user = userRepository.getOne(ownerUid);
            long totalUnread = 0;
            Object totalUnreadObj = redisTemplate.opsForValue().get(user.getUid() + Constants.TOTAL_UNREAD_SUFFIX);
            if (null != totalUnreadObj) {
                totalUnread = Long.parseLong((String) totalUnreadObj);
            }

            MessageContactVO contactVO = new MessageContactVO(user.getUid(), user.getUsername(), user.getAvatar(), totalUnread);
            contacts.stream().forEach(contact -> {
                Long mid = contact.getMid();
                MessageContent contentVO = contentRepository.findById(mid).orElse(null);
                User otherUser = userRepository.findById(contact.getOtherUid()).get();

                if (null != contentVO) {
                    long convUnread = 0;
                    Object convUnreadObj = redisTemplate.opsForHash().get(user.getUid() + Constants.CONVERSION_UNREAD_SUFFIX, otherUser.getUid());
                    if (null != convUnreadObj) {
                        convUnread = Long.parseLong((String) convUnreadObj);
                    }
                    MessageContactVO.ContactInfo contactInfo = contactVO.new ContactInfo(otherUser.getUid(), otherUser.getUsername(), otherUser.getAvatar(), mid, contact.getType(), contentVO.getContent(), convUnread, contact.getCreateTime());
                    contactVO.appendContact(contactInfo);
                }
            });
            return contactVO;
        }
        return null;
    }

    public long queryTotalUnread(long ownerUid) {
        long totalUnread = 0;
        Object totalUnreadObj = redisTemplate.opsForValue().get(ownerUid + Constants.TOTAL_UNREAD_SUFFIX);
        if (null != totalUnreadObj) {
            totalUnread = Long.parseLong((String) totalUnreadObj);
        }
        return totalUnread;
    }
}
