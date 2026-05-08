package com.cinema.controller;

import com.cinema.service.SeatService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/seats")
public class SeatController {
    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping
    public Map<String, Object> getSeatMap(@RequestParam Long showtimeId,
                                           @RequestParam Long hallId) {
        return seatService.getSeatMap(showtimeId, hallId);
    }
}
