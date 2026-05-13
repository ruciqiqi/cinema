package com.cinema.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "订单响应")
public class BookingResponse {
    
    @Schema(description = "订单ID", example = "1")
    private Long id;
    
    @Schema(description = "订单号", example = "BK202605130001")
    private String bookingCode;
    
    @Schema(description = "电影ID", example = "1")
    private Long movieId;
    
    @Schema(description = "电影名称", example = "复仇者联盟")
    private String movieTitle;
    
    @Schema(description = "场次ID", example = "1")
    private Long showtimeId;
    
    @Schema(description = "影厅ID", example = "1")
    private Long hallId;
    
    @Schema(description = "影厅名称", example = "1号厅")
    private String hallName;
    
    @Schema(description = "放映日期", example = "2026-05-15")
    private String showDate;
    
    @Schema(description = "放映时间", example = "14:30")
    private String showTime;
    
    @Schema(description = "座位标签列表", example = "[\"A1\", \"A2\"]")
    private List<String> seatLabels;
    
    @Schema(description = "用户姓名", example = "张三")
    private String userName;
    
    @Schema(description = "用户手机号", example = "13800138000")
    private String userPhone;
    
    @Schema(description = "总金额", example = "60.0")
    private Double totalPrice;
    
    @Schema(description = "实际支付金额", example = "60.0")
    private Double actualPaid;
    
    @Schema(description = "订单状态", example = "confirmed")
    private String status;
    
    @Schema(description = "创建时间", example = "2026-05-13 10:30:00")
    private String createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public Long getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(Long showtimeId) {
        this.showtimeId = showtimeId;
    }

    public Long getHallId() {
        return hallId;
    }

    public void setHallId(Long hallId) {
        this.hallId = hallId;
    }

    public String getHallName() {
        return hallName;
    }

    public void setHallName(String hallName) {
        this.hallName = hallName;
    }

    public String getShowDate() {
        return showDate;
    }

    public void setShowDate(String showDate) {
        this.showDate = showDate;
    }

    public String getShowTime() {
        return showTime;
    }

    public void setShowTime(String showTime) {
        this.showTime = showTime;
    }

    public List<String> getSeatLabels() {
        return seatLabels;
    }

    public void setSeatLabels(List<String> seatLabels) {
        this.seatLabels = seatLabels;
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

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Double getActualPaid() {
        return actualPaid;
    }

    public void setActualPaid(Double actualPaid) {
        this.actualPaid = actualPaid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}