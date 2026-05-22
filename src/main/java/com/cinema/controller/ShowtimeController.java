package com.cinema.controller;

import com.cinema.entity.Showtime;
import com.cinema.service.ShowtimeService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/showtimes")
public class ShowtimeController {
    private final ShowtimeService showtimeService;

    public ShowtimeController(ShowtimeService showtimeService) {
        this.showtimeService = showtimeService;
    }

    @GetMapping
    public Map<String, Object> list(@RequestParam(required = false) Long movieId,
                                     @RequestParam(required = false) String date) {
        Map<String, Object> result = new HashMap<>();
        List<Showtime> showtimes;
        if (movieId != null && date != null) {
            showtimes = showtimeService.getByMovieIdAndDate(movieId, date);
        } else if (movieId != null) {
            showtimes = showtimeService.getByMovieId(movieId);
        } else if (date != null) {
            showtimes = showtimeService.getByDate(date);
        } else {
            showtimes = showtimeService.getFutureShowtimes();
        }
        result.put("success", true);
        // Group by movieId for convenience
        Map<Long, List<Showtime>> grouped = new LinkedHashMap<>();
        for (Showtime s : showtimes) {
            grouped.computeIfAbsent(s.getMovieId(), k -> new ArrayList<>()).add(s);
        }
        result.put("data", showtimes);
        result.put("grouped", grouped);
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        Showtime showtime = showtimeService.getById(id);
        if (showtime == null) {
            result.put("success", false);
            result.put("message", "场次不存在");
        } else {
            result.put("success", true);
            result.put("data", showtime);
        }
        return result;
    }
}
