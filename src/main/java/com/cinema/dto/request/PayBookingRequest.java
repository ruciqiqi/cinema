package com.cinema.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "支付请求")
public class PayBookingRequest {
    @Schema(description = "订单号", required = true)
    private String bookingCode;
    
    @Schema(description = "支付方式", example = "alipay")
    private String paymentMethod;

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}