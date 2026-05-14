package com.cinema.controller;

import com.cinema.entity.Movie;
import com.cinema.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/movies")
@Tag(name = "电影管理", description = "电影列表、详情、搜索等接口")
public class MovieController {
    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    @Operation(summary = "获取电影列表", description = "获取所有电影或根据关键词搜索电影")
    public Map<String, Object> list(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
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
    @Operation(summary = "获取电影详情", description = "根据ID获取电影详细信息")
    public Map<String, Object> detail(
            @Parameter(description = "电影ID") @PathVariable Long id) {
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
