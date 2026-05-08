package com.cinema.controller;

import com.cinema.service.UserService;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/user")
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
    public Map<String, Object> profile(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return requireLogin();
        return userService.getProfile(userId);
    }

    @PutMapping("/profile")
    public Map<String, Object> updateProfile(@RequestBody Map<String, Object> body,
                                              HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return requireLogin();
        return userService.updateProfile(userId, body);
    }

    @PostMapping("/change-password")
    public Map<String, Object> changePassword(@RequestBody Map<String, Object> body,
                                               HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return requireLogin();
        String oldPwd = (String) body.get("oldPassword");
        String newPwd = (String) body.get("newPassword");
        return userService.changePassword(userId, oldPwd, newPwd);
    }

    @PostMapping("/real-name-auth")
    public Map<String, Object> realNameAuth(@RequestBody Map<String, Object> body,
                                             HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return requireLogin();
        String realName = (String) body.get("realName");
        String idCard = (String) body.get("idCard");
        return userService.realNameAuth(userId, realName, idCard);
    }

    @GetMapping("/points")
    public Map<String, Object> points(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return requireLogin();
        return userService.getPoints(userId);
    }

    @PostMapping("/redeem-points")
    public Map<String, Object> redeemPoints(@RequestBody Map<String, Object> body,
                                             HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return requireLogin();
        Integer points = body.get("points") != null ? Integer.valueOf(body.get("points").toString()) : 0;
        return userService.redeemPoints(userId, points);
    }
}
