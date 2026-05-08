package com.cinema.controller;

import com.cinema.entity.Movie;
import com.cinema.service.MovieService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/movies")
public class MovieController {
    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public Map<String, Object> list(@RequestParam(required = false) String keyword) {
        Map<String, Object> result = new HashMap<>();
        List<Movie> movies;
        if (keyword != null && !keyword.trim().isEmpty()) {
            movies = movieService.searchMovies(keyword.trim());
        } else {
            movies = movieService.getAllMovies();
        }
        result.put("success", true);
        result.put("data", movies);
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        Movie movie = movieService.getMovieById(id).orElse(null);
        if (movie == null) {
            result.put("success", false);
            result.put("message", "影片不存在");
        } else {
            result.put("success", true);
            result.put("data", movie);
        }
        return result;
    }
}
