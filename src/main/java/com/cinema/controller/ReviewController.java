package com.cinema.controller;

import com.cinema.service.ReviewService;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/movie/{movieId}")
    public Map<String, Object> byMovie(@PathVariable Long movieId) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", true);
        result.put("data", reviewService.getReviewsByMovie(movieId));
        result.put("stats", reviewService.getMovieRatingStats(movieId));
        return result;
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> body,
                                       HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        Long movieId = Long.valueOf(body.get("movieId").toString());
        Integer rating = Integer.valueOf(body.get("rating").toString());
        String content = (String) body.get("content");
        return reviewService.createReview(movieId, userId, username, rating, content);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        reviewService.deleteReview(id, userId);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", true);
        return result;
    }

    @GetMapping("/my")
    public Map<String, Object> myReviews(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", true);
        result.put("data", reviewService.getReviewsByUser(userId));
        return result;
    }
}
