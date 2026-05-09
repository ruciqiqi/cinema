package com.cinema.controller;

import com.cinema.service.BookingService;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> body,
                                       HttpServletRequest request) {
        Map<String, Object> err = new HashMap<>();
        if (body.get("showtimeId") == null) {
            err.put("success", false); err.put("message", "缺少场次ID"); return err;
        }
        Long showtimeId = Long.valueOf(body.get("showtimeId").toString());
        @SuppressWarnings("unchecked")
        List<Object> seatIdRaw = (List<Object>) body.get("seatIds");
        List<Long> seatIds = new ArrayList<>();
        if (seatIdRaw != null) {
            for (Object o : seatIdRaw) {
                seatIds.add(Long.valueOf(o.toString()));
            }
        }
        String userName = (String) body.get("userName");
        String userPhone = (String) body.get("userPhone");
        String snacksJson = (String) body.get("snacksJson");

        if (userName == null || userName.trim().isEmpty()) {
            err.put("success", false); err.put("message", "请输入姓名"); return err;
        }
        if (userPhone == null || userPhone.trim().isEmpty()) {
            err.put("success", false); err.put("message", "请输入手机号"); return err;
        }
        if (seatIds.isEmpty()) {
            err.put("success", false); err.put("message", "请选择座位"); return err;
        }

        // Get userId from JWT if logged in
        Long userId = (Long) request.getAttribute("userId");

        return bookingService.createBooking(showtimeId, seatIds, userName.trim(), userPhone.trim(),
                userId, snacksJson);
    }

    @GetMapping("/query")
    public Map<String, Object> query(@RequestParam String code) {
        return bookingService.getBookingByCode(code);
    }

    @GetMapping("/my")
    public Map<String, Object> myBookings(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        result.put("success", true);
        result.put("data", bookingService.getBookingsByUserId(userId));
        return result;
    }

    @PostMapping("/cancel")
    public Map<String, Object> cancel(@RequestBody Map<String, Object> body) {
        String code = (String) body.get("bookingCode");
        return bookingService.cancelBooking(code);
    }

    @GetMapping("/refund-preview")
    public Map<String, Object> refundPreview(@RequestParam String code) {
        return bookingService.getRefundPreview(code);
    }

    @PostMapping("/change")
    public Map<String, Object> change(@RequestBody Map<String, Object> body) {
        String bookingCode = (String) body.get("bookingCode");
        Long newShowtimeId = Long.valueOf(body.get("newShowtimeId").toString());
        @SuppressWarnings("unchecked")
        List<Object> seatIdRaw = (List<Object>) body.get("newSeatIds");
        List<Long> newSeatIds = new ArrayList<>();
        if (seatIdRaw != null) {
            for (Object o : seatIdRaw) newSeatIds.add(Long.valueOf(o.toString()));
        }
        return bookingService.changeTicket(bookingCode, newShowtimeId, newSeatIds);
    }

    @PostMapping("/pay")
    public Map<String, Object> pay(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String bookingCode = (String) body.get("bookingCode");
        String paymentMethod = (String) body.get("paymentMethod");
        return bookingService.processPayment(bookingCode, paymentMethod);
    }

    @GetMapping("/all")
    public Map<String, Object> allBookings() {
        return bookingService.getAllBookings();
    }
}
