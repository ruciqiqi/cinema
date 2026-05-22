package com.cinema.controller;

import com.cinema.entity.Cinema;
import com.cinema.entity.Hall;
import com.cinema.repository.CinemaRepository;
import com.cinema.repository.HallRepository;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class CinemaController {
    private final CinemaRepository cinemaRepository;
    private final HallRepository hallRepository;

    public CinemaController(CinemaRepository cinemaRepository, HallRepository hallRepository) {
        this.cinemaRepository = cinemaRepository;
        this.hallRepository = hallRepository;
    }

    @GetMapping("/cinemas")
    public Map<String, Object> list() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", cinemaRepository.findByStatus("open"));
        return result;
    }

    @GetMapping("/cinemas/{id}")
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

    @GetMapping("/halls")
    public Map<String, Object> listHalls() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Hall h : hallRepository.findAll()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", h.getId());
            m.put("name", h.getName());
            m.put("cinemaId", h.getCinemaId());
            m.put("hallType", h.getHallType());
            list.add(m);
        }
        result.put("success", true);
        result.put("data", list);
        return result;
    }
}
