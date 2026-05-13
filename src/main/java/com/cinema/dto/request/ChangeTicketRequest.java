package com.cinema.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "改签请求")
public class ChangeTicketRequest {
    @Schema(description = "原订单号", required = true)
    private String bookingCode;
    
    @Schema(description = "新场次ID", required = true)
    private Long newShowtimeId;
    
    @Schema(description = "新座位ID列表", required = true)
    private List<Long> newSeatIds;

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public Long getNewShowtimeId() {
        return newShowtimeId;
    }

    public void setNewShowtimeId(Long newShowtimeId) {
        this.newShowtimeId = newShowtimeId;
    }

    public List<Long> getNewSeatIds() {
        return newSeatIds;
    }

    public void setNewSeatIds(List<Long> newSeatIds) {
        this.newSeatIds = newSeatIds;
    }
}