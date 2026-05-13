package com.cinema.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "应用优惠券请求")
public class ApplyCouponRequest {
    @Schema(description = "用户优惠券ID", required = true)
    private Long userCouponId;
    
    @Schema(description = "订单金额", required = true)
    private Double orderAmount;

    public Long getUserCouponId() {
        return userCouponId;
    }

    public void setUserCouponId(Long userCouponId) {
        this.userCouponId = userCouponId;
    }

    public Double getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(Double orderAmount) {
        this.orderAmount = orderAmount;
    }
}