package com.cinema.controller;

import com.cinema.entity.Notification;
import com.cinema.repository.NotificationRepository;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public Map<String, Object> list(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            result.put("success", false); result.put("message", "请先登录"); return result;
        }
        List<Notification> list = notificationRepository.findByUserIdOrderByIdDesc(userId);
        long unread = notificationRepository.countByUserIdAndIsRead(userId, false);
        result.put("success", true);
        result.put("data", list);
        result.put("unreadCount", unread);
        return result;
    }

    @PutMapping("/{id}/read")
    public Map<String, Object> markRead(@PathVariable Long id, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            result.put("success", false); result.put("message", "请先登录"); return result;
        }
        notificationRepository.findById(id).ifPresent(n -> {
            if (n.getUserId().equals(userId)) {
                n.setIsRead(true);
                notificationRepository.save(n);
            }
        });
        result.put("success", true);
        return result;
    }

    @PutMapping("/read-all")
    public Map<String, Object> markAllRead(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            result.put("success", false); result.put("message", "请先登录"); return result;
        }
        List<Notification> list = notificationRepository.findByUserIdAndIsReadOrderByIdDesc(userId, false);
        for (Notification n : list) {
            n.setIsRead(true);
            notificationRepository.save(n);
        }
        result.put("success", true);
        return result;
    }
}
