package com.cinema.repository;

import com.cinema.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    List<Coupon> findByStatus(String status);
    Coupon findByCode(String code);
}
