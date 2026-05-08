package com.cinema.controller;

import com.cinema.service.AnnouncementService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {
    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping
    public Map<String, Object> list() {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", true);
        result.put("data", announcementService.getPublished());
        return result;
    }
}
