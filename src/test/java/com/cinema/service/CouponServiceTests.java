package com.cinema.service;

import com.cinema.entity.*;
import com.cinema.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTests {

    @Mock private CouponRepository couponRepository;
    @Mock private UserCouponRepository userCouponRepository;

    @InjectMocks
    private CouponService couponService;

    private Coupon discountCoupon;
    private Coupon cashCoupon;
    private UserCoupon userCoupon;

    @BeforeEach
    void setUp() {
        discountCoupon = new Coupon();
        discountCoupon.setId(1L);
        discountCoupon.setCode("DISC10");
        discountCoupon.setName("9折优惠券");
        discountCoupon.setType("discount");
        discountCoupon.setValue(10.0);
        discountCoupon.setMinAmount(50.0);
        discountCoupon.setStartDate(LocalDateTime.now().minusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        discountCoupon.setEndDate(LocalDateTime.now().plusDays(30)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        discountCoupon.setUsageLimit(100);
        discountCoupon.setUsedCount(10);
        discountCoupon.setStatus("active");

        cashCoupon = new Coupon();
        cashCoupon.setId(2L);
        cashCoupon.setCode("CASH20");
        cashCoupon.setName("20元现金券");
        cashCoupon.setType("cash");
        cashCoupon.setValue(20.0);
        cashCoupon.setMinAmount(100.0);
        cashCoupon.setStartDate(LocalDateTime.now().minusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        cashCoupon.setEndDate(LocalDateTime.now().plusDays(30)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        cashCoupon.setUsageLimit(100);
        cashCoupon.setUsedCount(0);
        cashCoupon.setStatus("active");

        userCoupon = new UserCoupon();
        userCoupon.setId(1L);
        userCoupon.setUserId(1L);
        userCoupon.setCouponId(1L);
        userCoupon.setStatus("unused");
        userCoupon.setReceivedAt(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    // ==================== CP-01 获取可用优惠券 ====================
    @Test
    void testGetAvailableCouponsFiltersCorrectly() {
        when(couponRepository.findByStatus("active"))
                .thenReturn(Arrays.asList(discountCoupon, cashCoupon));

        List<Coupon> result = couponService.getAvailableCoupons();

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(c -> c.getUsedCount() < c.getUsageLimit()));
    }

    // ==================== CP-02 过期优惠券过滤 ====================
    @Test
    void testGetAvailableCouponsExcludesExpired() {
        Coupon expired = new Coupon();
        expired.setId(3L);
        expired.setType("cash");
        expired.setValue(10.0);
        expired.setStatus("active");
        expired.setStartDate(LocalDateTime.now().minusDays(10)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        expired.setEndDate(LocalDateTime.now().minusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        expired.setUsedCount(0);
        expired.setUsageLimit(100);

        when(couponRepository.findByStatus("active"))
                .thenReturn(Arrays.asList(discountCoupon, expired));

        List<Coupon> result = couponService.getAvailableCoupons();

        assertEquals(1, result.size());
        assertEquals("DISC10", result.get(0).getCode());
    }

    // ==================== CP-03 已领完优惠券过滤 ====================
    @Test
    void testGetAvailableCouponsExcludesFullyUsed() {
        Coupon fullyUsed = new Coupon();
        fullyUsed.setId(4L);
        fullyUsed.setType("cash");
        fullyUsed.setValue(10.0);
        fullyUsed.setStatus("active");
        fullyUsed.setStartDate(LocalDateTime.now().minusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        fullyUsed.setEndDate(LocalDateTime.now().plusDays(30)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        fullyUsed.setUsedCount(100);
        fullyUsed.setUsageLimit(100);

        when(couponRepository.findByStatus("active"))
                .thenReturn(Arrays.asList(discountCoupon, fullyUsed));

        List<Coupon> result = couponService.getAvailableCoupons();

        assertEquals(1, result.size());
        assertEquals("DISC10", result.get(0).getCode());
    }

    // ==================== CP-04 领取优惠券 - 成功 ====================
    @Test
    void testReceiveCouponSuccess() {
        when(couponRepository.findById(1L)).thenReturn(Optional.of(discountCoupon));
        when(userCouponRepository.findByUserIdAndCouponId(1L, 1L)).thenReturn(null);
        when(userCouponRepository.save(any(UserCoupon.class))).thenReturn(userCoupon);
        when(couponRepository.save(any(Coupon.class))).thenReturn(discountCoupon);

        Map<String, Object> result = couponService.receiveCoupon(1L, 1L);

        assertTrue((Boolean) result.get("success"));
        assertEquals(11, discountCoupon.getUsedCount());
        verify(userCouponRepository, times(1)).save(any(UserCoupon.class));
    }

    // ==================== CP-05 领取优惠券 - 已领取过 ====================
    @Test
    void testReceiveCouponAlreadyReceived() {
        when(couponRepository.findById(1L)).thenReturn(Optional.of(discountCoupon));
        when(userCouponRepository.findByUserIdAndCouponId(1L, 1L)).thenReturn(userCoupon);

        Map<String, Object> result = couponService.receiveCoupon(1L, 1L);

        assertFalse((Boolean) result.get("success"));
        assertEquals("已领取过此优惠券", result.get("message"));
        verify(userCouponRepository, never()).save(any(UserCoupon.class));
    }

    // ==================== CP-06 领取优惠券 - 不存在 ====================
    @Test
    void testReceiveCouponNotFound() {
        when(couponRepository.findById(999L)).thenReturn(Optional.empty());

        Map<String, Object> result = couponService.receiveCoupon(1L, 999L);

        assertFalse((Boolean) result.get("success"));
        assertEquals("优惠券不存在", result.get("message"));
    }

    // ==================== CP-07 领取优惠券 - 已领完 ====================
    @Test
    void testReceiveCouponExhausted() {
        discountCoupon.setUsedCount(100);
        when(couponRepository.findById(1L)).thenReturn(Optional.of(discountCoupon));
        when(userCouponRepository.findByUserIdAndCouponId(1L, 1L)).thenReturn(null);

        Map<String, Object> result = couponService.receiveCoupon(1L, 1L);

        assertFalse((Boolean) result.get("success"));
        assertEquals("优惠券已被领完", result.get("message"));
    }

    // ==================== CP-08 查询用户优惠券 ====================
    @Test
    void testGetUserCouponsWithDetails() {
        UserCoupon uc1 = new UserCoupon();
        uc1.setId(1L);
        uc1.setUserId(1L);
        uc1.setCouponId(1L);
        uc1.setStatus("unused");

        UserCoupon uc2 = new UserCoupon();
        uc2.setId(2L);
        uc2.setUserId(1L);
        uc2.setCouponId(2L);
        uc2.setStatus("used");

        when(userCouponRepository.findByUserId(1L)).thenReturn(Arrays.asList(uc1, uc2));
        when(couponRepository.findById(1L)).thenReturn(Optional.of(discountCoupon));
        when(couponRepository.findById(2L)).thenReturn(Optional.of(cashCoupon));

        List<Map<String, Object>> result = couponService.getUserCoupons(1L);

        assertEquals(2, result.size());
        assertEquals("unused", result.get(0).get("status"));
        assertEquals("DISC10", result.get(0).get("code"));
        assertEquals("CASH20", result.get(1).get("code"));
    }

    // ==================== CP-09 应用折扣券 - 成功 ====================
    @Test
    void testApplyDiscountCoupon() {
        when(userCouponRepository.findById(1L)).thenReturn(Optional.of(userCoupon));
        when(couponRepository.findById(1L)).thenReturn(Optional.of(discountCoupon));

        Map<String, Object> result = couponService.applyCoupon(1L, 1L, 100.0);

        assertTrue((Boolean) result.get("success"));
        assertEquals(10.0, (Double) result.get("discount"), 0.01); // 100 * 10% = 10
    }

    // ==================== CP-10 应用现金券 - 成功 ====================
    @Test
    void testApplyCashCoupon() {
        UserCoupon ucCash = new UserCoupon();
        ucCash.setId(2L);
        ucCash.setUserId(1L);
        ucCash.setCouponId(2L);
        ucCash.setStatus("unused");

        when(userCouponRepository.findById(2L)).thenReturn(Optional.of(ucCash));
        when(couponRepository.findById(2L)).thenReturn(Optional.of(cashCoupon));

        Map<String, Object> result = couponService.applyCoupon(1L, 2L, 200.0);

        assertTrue((Boolean) result.get("success"));
        assertEquals(20.0, (Double) result.get("discount"), 0.01); // 固定20元
    }

    // ==================== CP-11 应用优惠券 - 金额不足 ====================
    @Test
    void testApplyCouponAmountNotEnough() {
        when(userCouponRepository.findById(1L)).thenReturn(Optional.of(userCoupon));
        when(couponRepository.findById(1L)).thenReturn(Optional.of(discountCoupon));

        // 最低消费50元，这里只有30元
        Map<String, Object> result = couponService.applyCoupon(1L, 1L, 30.0);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("message").toString().contains("不满"));
    }

    // ==================== CP-12 应用优惠券 - 已使用 ====================
    @Test
    void testApplyCouponAlreadyUsed() {
        UserCoupon used = new UserCoupon();
        used.setId(3L);
        used.setUserId(1L);
        used.setCouponId(2L);
        used.setStatus("used");

        when(userCouponRepository.findById(3L)).thenReturn(Optional.of(used));

        Map<String, Object> result = couponService.applyCoupon(1L, 3L, 200.0);

        assertFalse((Boolean) result.get("success"));
        assertEquals("优惠券已使用或已过期", result.get("message"));
    }

    // ==================== CP-13 应用优惠券 - 非本人 ====================
    @Test
    void testApplyCouponWrongUser() {
        UserCoupon other = new UserCoupon();
        other.setId(4L);
        other.setUserId(99L); // 其他用户
        other.setCouponId(1L);
        other.setStatus("unused");

        when(userCouponRepository.findById(4L)).thenReturn(Optional.of(other));

        Map<String, Object> result = couponService.applyCoupon(1L, 4L, 200.0);

        assertFalse((Boolean) result.get("success"));
        assertEquals("优惠券不存在", result.get("message"));
    }

    // ==================== CP-14 现金券 - 优惠金额不超过订单金额 ====================
    @Test
    void testApplyCashCouponCapAtOrderAmount() {
        UserCoupon ucCash = new UserCoupon();
        ucCash.setId(2L);
        ucCash.setUserId(1L);
        ucCash.setCouponId(2L);
        ucCash.setStatus("unused");

        // 小订单10元，现金券20元，最多优惠10元
        Coupon smallCash = new Coupon();
        smallCash.setId(2L);
        smallCash.setType("cash");
        smallCash.setValue(20.0);
        smallCash.setMinAmount(0.0);

        when(userCouponRepository.findById(2L)).thenReturn(Optional.of(ucCash));
        when(couponRepository.findById(2L)).thenReturn(Optional.of(smallCash));

        Map<String, Object> result = couponService.applyCoupon(1L, 2L, 10.0);

        assertTrue((Boolean) result.get("success"));
        assertEquals(10.0, (Double) result.get("discount"), 0.01);
    }
}
