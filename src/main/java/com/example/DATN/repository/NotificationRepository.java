package com.example.DATN.repository;

import com.example.DATN.entity.document.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification,String> {
    Slice<Notification> findByReceiverIdOrderByCreatedAtDesc(
            Long recipientId, Pageable pageable);

    long countByReceiverIdAndIsReadFalse(Long recipientId);

    // Đánh dấu tất cả đã đọc
    @Query("{ 'recipientId': ?0, 'isRead': false }")
    List<Notification> findUnreadByRecipientId(Long recipientId);
}
