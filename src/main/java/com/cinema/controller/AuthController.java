package com.cinema.controller;

import com.cinema.dto.request.LoginRequest;
import com.cinema.dto.request.RegisterRequest;
import com.cinema.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "用户认证", description = "用户注册、登录等认证相关接口")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建新用户账户")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "注册成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(description = "成功响应"),
                            examples = @ExampleObject(
                                    name = "成功",
                                    value = "{\"success\":true,\"message\":\"注册成功\",\"data\":{\"id\":1,\"username\":\"testuser\",\"nickname\":null,\"phone\":\"13800138000\",\"avatar\":null,\"role\":\"USER\",\"memberLevel\":1,\"points\":0,\"realNameAuth\":false,\"createdAt\":\"2026-05-13 10:00:00\"}}"
                            ))),
            @ApiResponse(responseCode = "200", description = "用户名已存在",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "失败",
                                    value = "{\"success\":false,\"message\":\"用户名已存在\",\"data\":null}"
                            )))
    })
    public Map<String, Object> register(@RequestBody RegisterRequest request) {
        return userService.register(request.getUsername(), request.getPassword(), request.getPhone());
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录获取访问令牌")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登录成功",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "成功",
                                    value = "{\"success\":true,\"message\":\"登录成功\",\"data\":{\"token\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\",\"username\":\"testuser\",\"role\":\"USER\"}}"
                            ))),
            @ApiResponse(responseCode = "200", description = "登录失败",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "失败",
                                    value = "{\"success\":false,\"message\":\"用户名或密码错误\",\"data\":null}"
                            )))
    })
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        return userService.login(request.getUsername(), request.getPassword());
    }
}
