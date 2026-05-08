package com.cinema.controller;

import com.cinema.entity.Cinema;
import com.cinema.repository.CinemaRepository;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/cinemas")
public class CinemaController {
    private final CinemaRepository cinemaRepository;

    public CinemaController(CinemaRepository cinemaRepository) {
        this.cinemaRepository = cinemaRepository;
    }

    @GetMapping
    public Map<String, Object> list() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", cinemaRepository.findByStatus("open"));
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        Cinema cinema = cinemaRepository.findById(id).orElse(null);
        if (cinema == null) {
            result.put("success", false); result.put("message", "影院不存在");
        } else {
            result.put("success", true); result.put("data", cinema);
        }
        return result;
    }
}
