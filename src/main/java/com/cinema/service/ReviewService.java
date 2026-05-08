package com.cinema.service;

import com.cinema.entity.Review;
import com.cinema.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public List<Review> getReviewsByMovie(Long movieId) {
        return reviewRepository.findByMovieIdAndStatus(movieId, "approved");
    }

    public List<Review> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    public Map<String, Object> createReview(Long movieId, Long userId, String userName,
                                             Integer rating, String content) {
        Map<String, Object> result = new HashMap<>();
        if (rating == null || rating < 1 || rating > 10) {
            result.put("success", false); result.put("message", "评分需在1-10之间"); return result;
        }
        if (content == null || content.trim().isEmpty()) {
            result.put("success", false); result.put("message", "请输入评论内容"); return result;
        }
        Review review = new Review();
        review.setMovieId(movieId);
        review.setUserId(userId);
        review.setUserName(userName);
        review.setRating(rating);
        review.setContent(content.trim());
        review.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        review.setStatus("approved");
        reviewRepository.save(review);
        result.put("success", true);
        result.put("data", review);
        return result;
    }

    public void deleteReview(Long reviewId, Long userId) {
        reviewRepository.findById(reviewId).ifPresent(r -> {
            if (r.getUserId().equals(userId)) {
                reviewRepository.delete(r);
            }
        });
    }

    public Map<String, Object> getMovieRatingStats(Long movieId) {
        Map<String, Object> stats = new HashMap<>();
        List<Review> reviews = reviewRepository.findByMovieId(movieId);
        if (reviews.isEmpty()) {
            stats.put("avgRating", 0);
            stats.put("count", 0);
        } else {
            double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0);
            stats.put("avgRating", Math.round(avg * 10.0) / 10.0);
            stats.put("count", reviews.size());
        }
        return stats;
    }
}
