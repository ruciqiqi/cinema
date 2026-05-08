package com.cinema.repository;

import com.cinema.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByIdDesc(Long userId);
    List<Notification> findByUserIdAndIsReadOrderByIdDesc(Long userId, Boolean isRead);
    long countByUserIdAndIsRead(Long userId, Boolean isRead);
}
