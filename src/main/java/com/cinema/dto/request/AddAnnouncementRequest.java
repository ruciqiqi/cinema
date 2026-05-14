package com.cinema.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "添加公告请求")
public class AddAnnouncementRequest {
    @Schema(description = "公告标题", required = true)
    private String title;
    
    @Schema(description = "公告内容", required = true)
    private String content;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}