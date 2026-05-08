package com.cinema.controller;

import com.cinema.service.CouponService;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {
    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping("/available")
    public Map<String, Object> available() {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", true);
        result.put("data", couponService.getAvailableCoupons());
        return result;
    }

    @PostMapping("/receive/{couponId}")
    public Map<String, Object> receive(@PathVariable Long couponId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return couponService.receiveCoupon(userId, couponId);
    }

    @GetMapping("/my")
    public Map<String, Object> myCoupons(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", true);
        result.put("data", couponService.getUserCoupons(userId));
        return result;
    }

    @PostMapping("/apply")
    public Map<String, Object> apply(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long userCouponId = Long.valueOf(body.get("userCouponId").toString());
        Double orderAmount = Double.valueOf(body.get("orderAmount").toString());
        return couponService.applyCoupon(userId, userCouponId, orderAmount);
    }
}
