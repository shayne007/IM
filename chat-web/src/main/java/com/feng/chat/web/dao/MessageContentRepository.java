package com.feng.chat.web.dao;

import com.feng.chat.web.entity.MessageContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageContentRepository extends JpaRepository<MessageContent, Long> {

}
