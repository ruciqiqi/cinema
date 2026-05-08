package com.cinema.repository;

import com.cinema.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
    List<UserCoupon> findByUserId(Long userId);
    List<UserCoupon> findByUserIdAndStatus(Long userId, String status);
    UserCoupon findByUserIdAndCouponId(Long userId, Long couponId);
}
