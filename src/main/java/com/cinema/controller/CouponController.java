package com.cinema.controller;

import com.cinema.dto.request.ApplyCouponRequest;
import com.cinema.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
@Tag(name = "优惠券管理", description = "优惠券领取、应用、查询等接口")
public class CouponController {
    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping("/available")
    @Operation(summary = "获取可用优惠券", description = "获取所有可领取的优惠券")
    public Map<String, Object> available() {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", true);
        result.put("data", couponService.getAvailableCoupons());
        return result;
    }

    @PostMapping("/receive/{couponId}")
    @Operation(summary = "领取优惠券", description = "用户领取指定优惠券")
    public Map<String, Object> receive(@PathVariable Long couponId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return couponService.receiveCoupon(userId, couponId);
    }

    @GetMapping("/my")
    @Operation(summary = "我的优惠券", description = "获取当前用户的优惠券列表")
    public Map<String, Object> myCoupons(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", true);
        result.put("data", couponService.getUserCoupons(userId));
        return result;
    }

    @PostMapping("/apply")
    @Operation(summary = "应用优惠券", description = "在订单中应用优惠券计算折扣")
    public Map<String, Object> apply(@RequestBody ApplyCouponRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return couponService.applyCoupon(userId, request.getUserCouponId(), request.getOrderAmount());
    }
}
