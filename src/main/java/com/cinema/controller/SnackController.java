package com.cinema.controller;

import com.cinema.entity.Snack;
import com.cinema.service.SnackService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/snacks")
public class SnackController {
    private final SnackService snackService;

    public SnackController(SnackService snackService) {
        this.snackService = snackService;
    }

    @GetMapping
    public Map<String, Object> list() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", snackService.getAvailableSnacks());
        return result;
    }
}
