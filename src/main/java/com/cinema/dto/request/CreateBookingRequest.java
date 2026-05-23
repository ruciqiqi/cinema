package com.cinema.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "创建订单请求")
public class CreateBookingRequest {
    @Schema(description = "场次ID", required = true)
    private Long showtimeId;
    
    @Schema(description = "座位ID列表", required = true)
    private List<Long> seatIds;
    
    @Schema(description = "用户姓名", required = true)
    private String userName;
    
    @Schema(description = "用户手机号", required = true)
    private String userPhone;
    
    @Schema(description = "零食JSON数据")
    private String snacksJson;

    @Schema(description = "用户优惠券ID")
    private Long userCouponId;

    @Schema(description = "优惠金额")
    private Double discountAmount;

    public Long getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(Long showtimeId) {
        this.showtimeId = showtimeId;
    }

    public List<Long> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(List<Long> seatIds) {
        this.seatIds = seatIds;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getSnacksJson() {
        return snacksJson;
    }

    public void setSnacksJson(String snacksJson) {
        this.snacksJson = snacksJson;
    }

    public Long getUserCouponId() { return userCouponId; }
    public void setUserCouponId(Long userCouponId) { this.userCouponId = userCouponId; }

    public Double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }
}