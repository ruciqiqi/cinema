package com.cinema.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "登录请求")
public class LoginRequest {

    @Schema(description = "用户名", example = "testuser", required = true)
    private String username;

    @Schema(description = "密码", example = "password123", required = true)
    private String password;

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
}