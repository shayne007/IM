package com.feng.chat.web.dao;

import com.feng.chat.web.entity.MessageRelation;
import com.feng.chat.web.entity.RelationMultiKeys;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRelationRepository extends JpaRepository<MessageRelation, RelationMultiKeys> {

    List<MessageRelation> findAllByOwnerUidAndOtherUidOrderByMidAsc(Long ownerUid, Long otherUid);

    List<MessageRelation> findAllByOwnerUidAndOtherUidAndMidIsGreaterThanOrderByMidAsc(Long ownerUid, Long otherUid, Long lastMid);
}
