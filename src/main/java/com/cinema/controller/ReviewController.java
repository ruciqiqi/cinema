package com.cinema.controller;

import com.cinema.dto.request.CreateReviewRequest;
import com.cinema.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "评论管理", description = "电影评论相关接口")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/movie/{movieId}")
    @Operation(summary = "获取电影评论", description = "获取指定电影的所有评论")
    public Map<String, Object> byMovie(@PathVariable Long movieId) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", true);
        result.put("data", reviewService.getReviewsByMovie(movieId));
        result.put("stats", reviewService.getMovieRatingStats(movieId));
        return result;
    }

    @PostMapping
    @Operation(summary = "创建评论", description = "用户发表电影评论")
    public Map<String, Object> create(@RequestBody CreateReviewRequest request,
                                       HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String username = (String) httpRequest.getAttribute("username");
        return reviewService.createReview(request.getMovieId(), userId, username, request.getRating(), request.getContent());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除评论", description = "删除自己的评论")
    public Map<String, Object> delete(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        reviewService.deleteReview(id, userId);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", true);
        return result;
    }

    @GetMapping("/my")
    @Operation(summary = "我的评论", description = "获取当前用户的评论列表")
    public Map<String, Object> myReviews(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", true);
        result.put("data", reviewService.getReviewsByUser(userId));
        return result;
    }
}
