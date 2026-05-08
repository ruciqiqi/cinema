package com.cinema.service;

import com.cinema.entity.Announcement;
import com.cinema.repository.AnnouncementRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AnnouncementService {
    private final AnnouncementRepository announcementRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    public List<Announcement> getPublished() {
        return announcementRepository.findByStatusOrderByIdDesc("published");
    }

    public List<Announcement> getAll() {
        return announcementRepository.findAll();
    }

    public Announcement create(String title, String content) {
        Announcement a = new Announcement();
        a.setTitle(title);
        a.setContent(content);
        a.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        a.setStatus("published");
        return announcementRepository.save(a);
    }

    public void delete(Long id) {
        announcementRepository.deleteById(id);
    }
}
