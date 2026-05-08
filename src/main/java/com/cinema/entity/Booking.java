package com.cinema.entity;

import javax.persistence.*;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "showtime_id")
    private Long showtimeId;

    @Column(name = "user_name", length = 50)
    private String userName;

    @Column(name = "user_phone", length = 20)
    private String userPhone;

    @Column(name = "total_price")
    private Double totalPrice;

    @Column(name = "booking_code", length = 20, unique = true)
    private String bookingCode;

    @Column(length = 20)
    private String status = "confirmed";

    @Column(length = 50)
    private String createdAt;

    @Column(length = 100)
    private String movieTitle;

    @Column(length = 50)
    private String hallName;

    @Column(length = 20)
    private String showDate;

    @Column(length = 20)
    private String showTime;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "snacks_json", length = 1000)
    private String snacksJson;

    @Column(name = "payment_method", length = 20)
    private String paymentMethod = "counter";

    @Column(name = "payment_status", length = 20)
    private String paymentStatus = "paid";

    @Column(name = "coupon_id")
    private Long couponId;

    @Column(name = "discount_amount")
    private Double discountAmount = 0.0;

    @Column(name = "actual_paid")
    private Double actualPaid;

    @Column(name = "ticket_qr_code", length = 200)
    private String ticketQrCode;

    @Column(name = "is_changed")
    private Boolean isChanged = false;

    @Column(name = "original_booking_id")
    private Long originalBookingId;

    @Column(name = "friend_phone", length = 20)
    private String friendPhone;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getShowtimeId() { return showtimeId; }
    public void setShowtimeId(Long showtimeId) { this.showtimeId = showtimeId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
    public String getBookingCode() { return bookingCode; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }
    public String getHallName() { return hallName; }
    public void setHallName(String hallName) { this.hallName = hallName; }
    public String getShowDate() { return showDate; }
    public void setShowDate(String showDate) { this.showDate = showDate; }
    public String getShowTime() { return showTime; }
    public void setShowTime(String showTime) { this.showTime = showTime; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getSnacksJson() { return snacksJson; }
    public void setSnacksJson(String snacksJson) { this.snacksJson = snacksJson; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public Long getCouponId() { return couponId; }
    public void setCouponId(Long couponId) { this.couponId = couponId; }
    public Double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }
    public Double getActualPaid() { return actualPaid; }
    public void setActualPaid(Double actualPaid) { this.actualPaid = actualPaid; }
    public String getTicketQrCode() { return ticketQrCode; }
    public void setTicketQrCode(String ticketQrCode) { this.ticketQrCode = ticketQrCode; }
    public Boolean getIsChanged() { return isChanged; }
    public void setIsChanged(Boolean isChanged) { this.isChanged = isChanged; }
    public Long getOriginalBookingId() { return originalBookingId; }
    public void setOriginalBookingId(Long originalBookingId) { this.originalBookingId = originalBookingId; }
    public String getFriendPhone() { return friendPhone; }
    public void setFriendPhone(String friendPhone) { this.friendPhone = friendPhone; }
}
