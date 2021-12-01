package com.feng.chat.web.service;

import com.feng.chat.web.Constants;
import com.feng.chat.web.dao.MessageContactRepository;
import com.feng.chat.web.dao.MessageContentRepository;
import com.feng.chat.web.dao.UserRepository;
import com.feng.chat.web.entity.MessageContact;
import com.feng.chat.web.entity.MessageContent;
import com.feng.chat.web.entity.User;
import com.feng.chat.web.vo.MessageContactVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MessageContactRepository contactRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MessageContentRepository contentRepository;

    public User login(String email, String password) {
        List<User> users = userRepository.findByEmail(email);
        if (null == users || users.isEmpty()) {
            log.warn("该用户不存在:" + email);
            throw new RuntimeException("该用户不存在:" + email);
        } else {
            User user = users.get(0);
            if (user.getPassword().equals(password)) {
                log.info(user.getUsername() + " logged in!");
                return user;
            } else {
                log.warn(user.getUsername() + " failed to log in!");
                throw new RuntimeException("invalid user info:" + user.getUsername());
            }
        }
    }


    public List<User> getAllUsersExcept(long exceptUid) {
        List<User> otherUsers = userRepository.findAll();
        otherUsers.remove(userRepository.getOne(exceptUid));
        return otherUsers;
    }

    public List<User> getAllUsersExcept(User exceptUser) {
        List<User> otherUsers = userRepository.findUsersByUidIsNot(exceptUser.getUid());
        return otherUsers;
    }

    public MessageContactVO getContacts(User ownerUser) {
        List<MessageContact> contacts = contactRepository.findMessageContactsByOwnerUidOrderByMidDesc(ownerUser.getUid());
        if (contacts != null) {
            long totalUnread = 0;
            Object totalUnreadObj = redisTemplate.opsForValue().get(ownerUser.getUid() + Constants.TOTAL_UNREAD_SUFFIX);
            if (null != totalUnreadObj) {
                totalUnread = Long.parseLong((String) totalUnreadObj);
            }

            final MessageContactVO contactVO = new MessageContactVO(ownerUser.getUid(), ownerUser.getUsername(), ownerUser.getAvatar(), totalUnread);
            contacts.stream().forEach(contact -> {
                Long mid = contact.getMid();
                MessageContent contentVO = contentRepository.getOne(mid);
                User otherUser = userRepository.getOne(contact.getOtherUid());

                if (null != contentVO) {
                    long convUnread = 0;
                    Object convUnreadObj = redisTemplate.opsForHash().get(ownerUser.getUid() + Constants.CONVERSION_UNREAD_SUFFIX, otherUser.getUid());
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
}
