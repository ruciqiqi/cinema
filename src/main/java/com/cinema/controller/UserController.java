package com.cinema.controller;

import com.cinema.dto.request.ChangePasswordRequest;
import com.cinema.dto.request.RealNameAuthRequest;
import com.cinema.dto.request.RedeemPointsRequest;
import com.cinema.dto.request.UpdateProfileRequest;
import com.cinema.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户信息、积分、密码修改等接口")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private Long getUserId(HttpServletRequest request) {
        Object uid = request.getAttribute("userId");
        if (uid == null) return null;
        return ((Number) uid).longValue();
    }

    private Map<String, Object> requireLogin() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "请先登录");
        return result;
    }

    @GetMapping("/profile")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的个人信息")
    public Map<String, Object> profile(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return requireLogin();
        return userService.getProfile(userId);
    }

    @PutMapping("/profile")
    @Operation(summary = "更新用户信息", description = "更新用户个人资料")
    public Map<String, Object> updateProfile(@RequestBody UpdateProfileRequest request,
                                              HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        if (userId == null) return requireLogin();
        
        Map<String, Object> body = new HashMap<>();
        if (request.getNickname() != null) body.put("nickname", request.getNickname());
        if (request.getPhone() != null) body.put("phone", request.getPhone());
        if (request.getAvatar() != null) body.put("avatar", request.getAvatar());
        
        return userService.updateProfile(userId, body);
    }

    @PostMapping("/change-password")
    @Operation(summary = "修改密码", description = "修改用户登录密码")
    public Map<String, Object> changePassword(@RequestBody ChangePasswordRequest request,
                                               HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        if (userId == null) return requireLogin();
        return userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
    }

    @PostMapping("/real-name-auth")
    @Operation(summary = "实名认证", description = "用户实名认证")
    public Map<String, Object> realNameAuth(@RequestBody RealNameAuthRequest request,
                                             HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        if (userId == null) return requireLogin();
        return userService.realNameAuth(userId, request.getRealName(), request.getIdCard());
    }

    @GetMapping("/points")
    @Operation(summary = "获取积分", description = "获取用户积分信息")
    public Map<String, Object> points(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return requireLogin();
        return userService.getPoints(userId);
    }

    @PostMapping("/redeem-points")
    @Operation(summary = "积分兑换", description = "使用积分兑换代金券")
    public Map<String, Object> redeemPoints(@RequestBody RedeemPointsRequest request,
                                             HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        if (userId == null) return requireLogin();
        return userService.redeemPoints(userId, request.getPoints());
    }
}
