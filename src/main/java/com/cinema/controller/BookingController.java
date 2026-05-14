package com.cinema.controller;

import com.cinema.dto.request.CancelBookingRequest;
import com.cinema.dto.request.ChangeTicketRequest;
import com.cinema.dto.request.CreateBookingRequest;
import com.cinema.dto.request.PayBookingRequest;
import com.cinema.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "订单管理", description = "订单创建、查询、取消、改签、退款等接口")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @Operation(summary = "创建订单", description = "创建新的电影票订单")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(description = "成功响应"),
                            examples = @ExampleObject(
                                    name = "成功",
                                    value = "{\"success\":true,\"message\":\"订单创建成功\",\"data\":{\"id\":1,\"bookingCode\":\"BK202605130001\",\"movieId\":1,\"movieTitle\":\"复仇者联盟\",\"showtimeId\":1,\"hallId\":1,\"hallName\":\"1号厅\",\"showDate\":\"2026-05-15\",\"showTime\":\"14:30\",\"seatLabels\":[\"A1\",\"A2\"],\"userName\":\"张三\",\"userPhone\":\"13800138000\",\"totalPrice\":60.0,\"actualPaid\":60.0,\"status\":\"pending\",\"createdAt\":\"2026-05-13 10:30:00\"}}"
                            ))),
            @ApiResponse(responseCode = "200", description = "创建失败",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "失败",
                                    value = "{\"success\":false,\"message\":\"缺少场次ID\",\"data\":null}"
                            )))
    })
    public Map<String, Object> create(@RequestBody CreateBookingRequest request,
                                       HttpServletRequest httpRequest) {
        Map<String, Object> err = new HashMap<>();
        if (request.getShowtimeId() == null) {
            err.put("success", false); err.put("message", "缺少场次ID"); return err;
        }
        if (request.getUserName() == null || request.getUserName().trim().isEmpty()) {
            err.put("success", false); err.put("message", "请输入姓名"); return err;
        }
        if (request.getUserPhone() == null || request.getUserPhone().trim().isEmpty()) {
            err.put("success", false); err.put("message", "请输入手机号"); return err;
        }
        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            err.put("success", false); err.put("message", "请选择座位"); return err;
        }

        Long userId = (Long) httpRequest.getAttribute("userId");

        return bookingService.createBooking(request.getShowtimeId(), request.getSeatIds(), 
                request.getUserName().trim(), request.getUserPhone().trim(),
                userId, request.getSnacksJson());
    }

    @GetMapping("/query")
    @Operation(summary = "查询订单", description = "根据订单号查询订单详情")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "成功",
                                    value = "{\"success\":true,\"message\":\"查询成功\",\"data\":{\"id\":1,\"bookingCode\":\"BK202605130001\",\"movieTitle\":\"复仇者联盟\",\"hallName\":\"1号厅\",\"showDate\":\"2026-05-15\",\"showTime\":\"14:30\",\"userName\":\"张三\",\"userPhone\":\"13800138000\",\"totalPrice\":60.0,\"status\":\"confirmed\",\"seatLabels\":[\"A1\",\"A2\"]}}"
                            ))),
            @ApiResponse(responseCode = "200", description = "订单不存在",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "失败",
                                    value = "{\"success\":false,\"message\":\"订单不存在\",\"data\":null}"
                            )))
    })
    public Map<String, Object> query(@Parameter(description = "订单号") @RequestParam String code) {
        return bookingService.getBookingByCode(code);
    }

    @GetMapping("/my")
    @Operation(summary = "我的订单", description = "获取当前登录用户的订单列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "成功",
                                    value = "{\"success\":true,\"data\":[{\"id\":1,\"bookingCode\":\"BK202605130001\",\"movieTitle\":\"复仇者联盟\",\"hallName\":\"1号厅\",\"showDate\":\"2026-05-15\",\"showTime\":\"14:30\",\"status\":\"confirmed\",\"totalPrice\":60.0}]}"
                            ))),
            @ApiResponse(responseCode = "200", description = "未登录",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "未登录",
                                    value = "{\"success\":false,\"message\":\"请先登录\",\"data\":null}"
                            )))
    })
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
    @Operation(summary = "取消订单", description = "取消未支付或已支付的订单")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "取消成功",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "成功",
                                    value = "{\"success\":true,\"message\":\"订单已取消\",\"data\":null}"
                            ))),
            @ApiResponse(responseCode = "200", description = "取消失败",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "失败",
                                    value = "{\"success\":false,\"message\":\"订单不存在或已过期\",\"data\":null}"
                            )))
    })
    public Map<String, Object> cancel(@RequestBody CancelBookingRequest request) {
        return bookingService.cancelBooking(request.getBookingCode());
    }

    @GetMapping("/refund-preview")
    @Operation(summary = "退款预览", description = "查看订单退款金额和规则")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "预览成功",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "成功",
                                    value = "{\"success\":true,\"message\":\"查询成功\",\"data\":{\"bookingCode\":\"BK202605130001\",\"totalAmount\":60.0,\"refundAmount\":54.0,\"refundRate\":0.9,\"reason\":\"开场前2小时内退款扣10%\"}}"
                            )))
    })
    public Map<String, Object> refundPreview(@Parameter(description = "订单号") @RequestParam String code) {
        return bookingService.getRefundPreview(code);
    }

    @PostMapping("/change")
    @Operation(summary = "改签", description = "更改订单的场次和座位")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "改签成功",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "成功",
                                    value = "{\"success\":true,\"message\":\"改签成功\",\"data\":{\"bookingCode\":\"BK202605130001\",\"newShowDate\":\"2026-05-16\",\"newShowTime\":\"16:00\",\"newSeatLabels\":[\"B1\",\"B2\"]}}"
                            ))),
            @ApiResponse(responseCode = "200", description = "改签失败",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "失败",
                                    value = "{\"success\":false,\"message\":\"该场次已过开场时间，无法改签\",\"data\":null}"
                            )))
    })
    public Map<String, Object> change(@RequestBody ChangeTicketRequest request) {
        return bookingService.changeTicket(request.getBookingCode(), request.getNewShowtimeId(), request.getNewSeatIds());
    }

    @PostMapping("/pay")
    @Operation(summary = "支付", description = "支付订单")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "支付成功",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "成功",
                                    value = "{\"success\":true,\"message\":\"支付成功\",\"data\":{\"bookingCode\":\"BK202605130001\",\"status\":\"confirmed\",\"actualPaid\":60.0}}"
                            ))),
            @ApiResponse(responseCode = "200", description = "支付失败",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "失败",
                                    value = "{\"success\":false,\"message\":\"支付失败，请重试\",\"data\":null}"
                            )))
    })
    public Map<String, Object> pay(@RequestBody PayBookingRequest request, HttpServletRequest httpRequest) {
        return bookingService.processPayment(request.getBookingCode(), request.getPaymentMethod());
    }

    @GetMapping("/all")
    @Operation(summary = "所有订单", description = "获取所有订单（管理员）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "成功",
                                    value = "{\"success\":true,\"data\":[{\"id\":1,\"bookingCode\":\"BK202605130001\",\"movieTitle\":\"复仇者联盟\",\"hallName\":\"1号厅\",\"showDate\":\"2026-05-15\",\"showTime\":\"14:30\",\"userName\":\"张三\",\"totalPrice\":60.0,\"status\":\"confirmed\",\"seatLabels\":[\"A1\",\"A2\"]}]}"
                            )))
    })
    public Map<String, Object> allBookings() {
        return bookingService.getAllBookings();
    }
}
