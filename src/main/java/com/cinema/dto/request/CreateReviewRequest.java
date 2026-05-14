package com.cinema.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "创建评论请求")
public class CreateReviewRequest {
    @Schema(description = "电影ID", required = true)
    private Long movieId;
    
    @Schema(description = "评分（1-5）", required = true)
    private Integer rating;
    
    @Schema(description = "评论内容")
    private String content;

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}