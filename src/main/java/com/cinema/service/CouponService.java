package com.cinema.service;

import com.cinema.entity.*;
import com.cinema.repository.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CouponService {
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;

    public CouponService(CouponRepository couponRepository, UserCouponRepository userCouponRepository) {
        this.couponRepository = couponRepository;
        this.userCouponRepository = userCouponRepository;
    }

    public List<Coupon> getAvailableCoupons() {
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<Coupon> all = couponRepository.findByStatus("active");
        List<Coupon> result = new ArrayList<>();
        for (Coupon c : all) {
            if (c.getStartDate().compareTo(today) <= 0 && c.getEndDate().compareTo(today) >= 0
                    && c.getUsedCount() < c.getUsageLimit()) {
                result.add(c);
            }
        }
        return result;
    }

    public Map<String, Object> receiveCoupon(Long userId, Long couponId) {
        Map<String, Object> result = new HashMap<>();
        Coupon coupon = couponRepository.findById(couponId).orElse(null);
        if (coupon == null) {
            result.put("success", false); result.put("message", "优惠券不存在"); return result;
        }
        UserCoupon existing = userCouponRepository.findByUserIdAndCouponId(userId, couponId);
        if (existing != null) {
            result.put("success", false); result.put("message", "已领取过此优惠券"); return result;
        }
        if (coupon.getUsedCount() >= coupon.getUsageLimit()) {
            result.put("success", false); result.put("message", "优惠券已被领完"); return result;
        }
        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setCouponId(couponId);
        uc.setStatus("unused");
        uc.setReceivedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        userCouponRepository.save(uc);
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);
        result.put("success", true);
        result.put("message", "领取成功");
        return result;
    }

    public List<Map<String, Object>> getUserCoupons(Long userId) {
        List<Map<String, Object>> list = new ArrayList<>();
        List<UserCoupon> ucs = userCouponRepository.findByUserId(userId);
        for (UserCoupon uc : ucs) {
            Coupon c = couponRepository.findById(uc.getCouponId()).orElse(null);
            if (c == null) continue;
            Map<String, Object> m = new HashMap<>();
            m.put("userCouponId", uc.getId());
            m.put("id", uc.getId());
            m.put("couponId", c.getId());
            m.put("code", c.getCode());
            m.put("name", c.getName());
            m.put("type", c.getType());
            m.put("value", c.getValue());
            m.put("discount", "discount".equals(c.getType()) ? 0 : c.getValue());
            m.put("minAmount", c.getMinAmount());
            m.put("endDate", c.getEndDate());
            m.put("status", uc.getStatus());
            list.add(m);
        }
        return list;
    }

    public Map<String, Object> applyCoupon(Long userId, Long userCouponId, Double orderAmount) {
        Map<String, Object> result = new HashMap<>();
        UserCoupon uc = userCouponRepository.findById(userCouponId).orElse(null);
        if (uc == null || !uc.getUserId().equals(userId)) {
            result.put("success", false); result.put("message", "优惠券不存在"); return result;
        }
        if (!"unused".equals(uc.getStatus())) {
            result.put("success", false); result.put("message", "优惠券已使用或已过期"); return result;
        }
        Coupon c = couponRepository.findById(uc.getCouponId()).orElse(null);
        if (c == null) {
            result.put("success", false); result.put("message", "优惠券不存在"); return result;
        }
        if (orderAmount < c.getMinAmount()) {
            result.put("success", false);
            result.put("message", "订单金额不满" + c.getMinAmount() + "元，无法使用"); return result;
        }
        double discount = 0;
        if ("discount".equals(c.getType())) {
            discount = orderAmount * (c.getValue() / 100.0);
        } else {
            discount = Math.min(c.getValue(), orderAmount);
        }
        discount = Math.round(discount * 100.0) / 100.0;
        result.put("success", true);
        result.put("discount", discount);
        result.put("couponId", c.getId());
        result.put("userCouponId", uc.getId());
        return result;
    }
}
