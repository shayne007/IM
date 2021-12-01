package com.feng.chat.web.dao;

import com.feng.chat.web.entity.ContactMultiKeys;
import com.feng.chat.web.entity.MessageContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageContactRepository extends JpaRepository<MessageContact, ContactMultiKeys> {

    public List<MessageContact> findMessageContactsByOwnerUidOrderByMidDesc(Long ownerUid);
}
