package com.cinema.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "注册请求")
public class RegisterRequest {

    @Schema(description = "用户名（至少3个字符）", example = "testuser", required = true)
    private String username;

    @Schema(description = "密码（至少6个字符）", example = "password123", required = true)
    private String password;

    @Schema(description = "手机号", example = "13800138000", required = true)
    private String phone;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}