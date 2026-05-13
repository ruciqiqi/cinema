package com.cinema.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户响应")
public class UserResponse {
    
    @Schema(description = "用户ID", example = "1")
    private Long id;
    
    @Schema(description = "用户名", example = "testuser")
    private String username;
    
    @Schema(description = "昵称", example = "张三")
    private String nickname;
    
    @Schema(description = "手机号", example = "13800138000")
    private String phone;
    
    @Schema(description = "头像URL")
    private String avatar;
    
    @Schema(description = "角色", example = "USER")
    private String role;
    
    @Schema(description = "会员等级", example = "1")
    private Integer memberLevel;
    
    @Schema(description = "积分", example = "100")
    private Integer points;
    
    @Schema(description = "是否实名认证", example = "false")
    private Boolean realNameAuth;
    
    @Schema(description = "创建时间", example = "2026-05-13 10:00:00")
    private String createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getMemberLevel() {
        return memberLevel;
    }

    public void setMemberLevel(Integer memberLevel) {
        this.memberLevel = memberLevel;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Boolean getRealNameAuth() {
        return realNameAuth;
    }

    public void setRealNameAuth(Boolean realNameAuth) {
        this.realNameAuth = realNameAuth;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}