package com.cinema.service;

import com.cinema.entity.*;
import com.cinema.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTests {

    @Mock private BookingRepository bookingRepository;
    @Mock private BookingSeatRepository bookingSeatRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private ShowtimeRepository showtimeRepository;
    @Mock private MovieRepository movieRepository;
    @Mock private SnackRepository snackRepository;
    @Mock private RefundRuleRepository refundRuleRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private UserCouponRepository userCouponRepository;
    @Mock private SeatService seatService;

    @InjectMocks
    private BookingService bookingService;

    private Showtime showtime;
    private Movie movie;
    private Seat seat1;
    private Seat seat2;
    private Seat seatVip;
    private User user;

    @BeforeEach
    void setUp() {
        showtime = new Showtime();
        showtime.setId(1L);
        showtime.setMovieId(1L);
        showtime.setHallId(1L);
        showtime.setHallName("1号激光IMAX厅");
        showtime.setShowDate(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        showtime.setShowTime("14:30");
        showtime.setPriceStandard(39.9);
        showtime.setPriceVip(59.9);

        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("测试电影");
        movie.setStatus("showing");

        seat1 = new Seat();
        seat1.setId(101L);
        seat1.setHallId(1L);
        seat1.setRowLabel("A");
        seat1.setSeatNum(1);
        seat1.setSeatType("standard");

        seat2 = new Seat();
        seat2.setId(102L);
        seat2.setHallId(1L);
        seat2.setRowLabel("A");
        seat2.setSeatNum(2);
        seat2.setSeatType("standard");

        seatVip = new Seat();
        seatVip.setId(201L);
        seatVip.setHallId(1L);
        seatVip.setRowLabel("V");
        seatVip.setSeatNum(1);
        seatVip.setSeatType("vip");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setTotalSpent(100.0);
        user.setPoints(100);
        user.setMemberLevel(1);
    }

    // ==================== BS-01 创建订单 - 成功 ====================
    @Test
    void testCreateBookingSuccess() {
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(seatService.getBookedSeatIds(1L)).thenReturn(new HashSet<>());
        when(seatRepository.findById(101L)).thenReturn(Optional.of(seat1));
        when(seatRepository.findById(102L)).thenReturn(Optional.of(seat2));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = bookingService.createBooking(
                1L, Arrays.asList(101L, 102L), "张三", "13800000000",
                1L, null, null, null);

        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("bookingCode"));
        double total = (Double) result.get("totalPrice");
        assertEquals(79.8, total, 0.01);
        @SuppressWarnings("unchecked")
        List<String> labels = (List<String>) result.get("seatLabels");
        assertEquals(2, labels.size());
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(bookingSeatRepository, times(2)).save(any(BookingSeat.class));
    }

    // ==================== BS-02 创建订单 - 场次不存在 ====================
    @Test
    void testCreateBookingShowtimeNotFound() {
        when(showtimeRepository.findById(999L)).thenReturn(Optional.empty());

        Map<String, Object> result = bookingService.createBooking(
                999L, Arrays.asList(101L), "张三", "13800000000",
                1L, null, null, null);

        assertFalse((Boolean) result.get("success"));
        assertEquals("场次不存在", result.get("message"));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    // ==================== BS-03 创建订单 - 座位已被预订 ====================
    @Test
    void testCreateBookingSeatAlreadyBooked() {
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        Set<Long> booked = new HashSet<>();
        booked.add(101L);
        when(seatService.getBookedSeatIds(1L)).thenReturn(booked);

        Map<String, Object> result = bookingService.createBooking(
                1L, Arrays.asList(101L), "张三", "13800000000",
                1L, null, null, null);

        assertFalse((Boolean) result.get("success"));
        assertEquals("座位已被预订，请重新选择", result.get("message"));
    }

    // ==================== BS-04 创建订单 - 场次已过期 ====================
    @Test
    void testCreateBookingShowtimeExpired() {
        Showtime pastShowtime = new Showtime();
        pastShowtime.setId(2L);
        pastShowtime.setShowDate(LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        pastShowtime.setShowTime("10:00");
        when(showtimeRepository.findById(2L)).thenReturn(Optional.of(pastShowtime));

        Map<String, Object> result = bookingService.createBooking(
                2L, Arrays.asList(101L), "张三", "13800000000",
                1L, null, null, null);

        assertFalse((Boolean) result.get("success"));
        assertEquals("该场次已过期，无法购买", result.get("message"));
    }

    // ==================== BS-05 创建订单 - VIP 座位按 VIP 价格 ====================
    @Test
    void testCreateBookingVipSeatPricing() {
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(seatService.getBookedSeatIds(1L)).thenReturn(new HashSet<>());
        when(seatRepository.findById(201L)).thenReturn(Optional.of(seatVip));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = bookingService.createBooking(
                1L, Arrays.asList(201L), "张三", "13800000000",
                1L, null, null, null);

        assertTrue((Boolean) result.get("success"));
        assertEquals(59.9, (Double) result.get("totalPrice"), 0.01);
    }

    // ==================== BS-06 创建订单 - 普通座位按标准价格 ====================
    @Test
    void testCreateBookingStandardSeatPricing() {
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(seatService.getBookedSeatIds(1L)).thenReturn(new HashSet<>());
        when(seatRepository.findById(101L)).thenReturn(Optional.of(seat1));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = bookingService.createBooking(
                1L, Arrays.asList(101L), "张三", "13800000000",
                1L, null, null, null);

        assertTrue((Boolean) result.get("success"));
        assertEquals(39.9, (Double) result.get("totalPrice"), 0.01);
    }

    // ==================== BS-07 创建订单 - 附带小食 ====================
    @Test
    void testCreateBookingWithSnacks() {
        Snack snack1 = new Snack();
        snack1.setId(1L);
        snack1.setName("爆米花");
        snack1.setPrice(20.0);
        Snack snack2 = new Snack();
        snack2.setId(2L);
        snack2.setName("可乐");
        snack2.setPrice(10.0);

        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(seatService.getBookedSeatIds(1L)).thenReturn(new HashSet<>());
        when(seatRepository.findById(101L)).thenReturn(Optional.of(seat1));
        when(snackRepository.findById(1L)).thenReturn(Optional.of(snack1));
        when(snackRepository.findById(2L)).thenReturn(Optional.of(snack2));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = bookingService.createBooking(
                1L, Arrays.asList(101L), "张三", "13800000000",
                1L, "1:2,2:1", null, null);

        assertTrue((Boolean) result.get("success"));
        // seat(39.9) + 爆米花x2(40) + 可乐x1(10) = 89.9
        assertEquals(89.9, (Double) result.get("totalPrice"), 0.01);
    }

    // ==================== BS-08 创建订单 - 未登录用户 ====================
    @Test
    void testCreateBookingGuestUser() {
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(seatService.getBookedSeatIds(1L)).thenReturn(new HashSet<>());
        when(seatRepository.findById(101L)).thenReturn(Optional.of(seat1));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = bookingService.createBooking(
                1L, Arrays.asList(101L), "游客", "13900000000",
                null, null, null, null);

        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("bookingCode"));
    }

    // ==================== BS-09 查询订单 - 成功 ====================
    @Test
    void testGetBookingByCodeSuccess() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBookingCode("CIN202606010001");
        booking.setShowtimeId(1L);
        booking.setMovieTitle("测试电影");
        booking.setStatus("confirmed");

        BookingSeat bs1 = new BookingSeat();
        bs1.setId(1L);
        bs1.setBookingId(1L);
        bs1.setSeatId(101L);
        bs1.setSeatLabel("A排1座");

        when(bookingRepository.findByBookingCode("CIN202606010001")).thenReturn(booking);
        when(bookingSeatRepository.findByBookingId(1L)).thenReturn(Arrays.asList(bs1));

        Map<String, Object> result = bookingService.getBookingByCode("CIN202606010001");

        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("booking"));
        @SuppressWarnings("unchecked")
        List<String> labels = (List<String>) result.get("seatLabels");
        assertEquals(1, labels.size());
        assertEquals("A排1座", labels.get(0));
    }

    // ==================== BS-10 查询订单 - 订单号不存在 ====================
    @Test
    void testGetBookingByCodeNotFound() {
        when(bookingRepository.findByBookingCode("INVALID")).thenReturn(null);

        Map<String, Object> result = bookingService.getBookingByCode("INVALID");

        assertFalse((Boolean) result.get("success"));
        assertEquals("订单不存在", result.get("message"));
    }

    // ==================== BS-11 查询我的订单 - 有数据 ====================
    @Test
    void testGetBookingsByUserIdWithData() {
        Booking booking1 = new Booking();
        booking1.setId(1L);
        booking1.setUserId(1L);
        booking1.setMovieTitle("电影A");
        booking1.setCreatedAt("2026-06-12 14:30:00");

        Booking booking2 = new Booking();
        booking2.setId(2L);
        booking2.setUserId(1L);
        booking2.setMovieTitle("电影B");
        booking2.setCreatedAt("2026-06-11 14:30:00");

        when(bookingRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Arrays.asList(booking1, booking2));
        when(bookingSeatRepository.findByBookingId(anyLong())).thenReturn(new ArrayList<>());

        List<Map<String, Object>> list = bookingService.getBookingsByUserId(1L);

        assertEquals(2, list.size());
        assertEquals("电影A", list.get(0).get("movieTitle"));
    }

    // ==================== BS-12 查询我的订单 - 空列表 ====================
    @Test
    void testGetBookingsByUserIdEmpty() {
        when(bookingRepository.findByUserIdOrderByCreatedAtDesc(999L)).thenReturn(new ArrayList<>());

        List<Map<String, Object>> list = bookingService.getBookingsByUserId(999L);

        assertTrue(list.isEmpty());
    }

    // ==================== BS-13 取消订单 - 成功 ====================
    @Test
    void testCancelBookingSuccess() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBookingCode("CIN202606010001");
        booking.setStatus("confirmed");
        booking.setShowtimeId(1L);
        booking.setTotalPrice(79.8);
        booking.setUserId(1L);

        when(bookingRepository.findByBookingCode("CIN202606010001")).thenReturn(booking);
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        RefundRule rule = new RefundRule();
        rule.setHoursBeforeShow(2);
        rule.setRefundRate(1.0);
        rule.setDescription("开场2小时前全额退款");
        when(refundRuleRepository.findAll()).thenReturn(Arrays.asList(rule));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Map<String, Object> result = bookingService.cancelBooking("CIN202606010001");

        assertTrue((Boolean) result.get("success"));
        assertEquals("cancelled", booking.getStatus());
        assertNotNull(result.get("refundAmount"));
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    // ==================== BS-14 取消订单 - 重复取消 ====================
    @Test
    void testCancelBookingAlreadyCancelled() {
        Booking cancelledBooking = new Booking();
        cancelledBooking.setBookingCode("CIN202606010001");
        cancelledBooking.setStatus("cancelled");

        when(bookingRepository.findByBookingCode("CIN202606010001")).thenReturn(cancelledBooking);

        Map<String, Object> result = bookingService.cancelBooking("CIN202606010001");

        assertFalse((Boolean) result.get("success"));
        assertEquals("订单已取消，无法重复操作", result.get("message"));
    }

    // ==================== BS-15 取消订单 - 订单不存在 ====================
    @Test
    void testCancelBookingNotFound() {
        when(bookingRepository.findByBookingCode("INVALID")).thenReturn(null);

        Map<String, Object> result = bookingService.cancelBooking("INVALID");

        assertFalse((Boolean) result.get("success"));
        assertEquals("订单不存在", result.get("message"));
    }

    // ==================== BS-17 退款预览 - 24小时前全额 ====================
    @Test
    void testGetRefundPreviewFullRefund() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBookingCode("FUTURE");
        booking.setStatus("confirmed");
        booking.setShowtimeId(1L);
        booking.setTotalPrice(100.0);

        Showtime futureShowtime = new Showtime();
        futureShowtime.setShowDate(LocalDateTime.now().plusDays(3).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        futureShowtime.setShowTime("14:30");

        when(bookingRepository.findByBookingCode("FUTURE")).thenReturn(booking);
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(futureShowtime));
        RefundRule fullRule = new RefundRule();
        fullRule.setHoursBeforeShow(24);
        fullRule.setRefundRate(1.0);
        fullRule.setDescription("开场24小时前全额退款");
        when(refundRuleRepository.findAll()).thenReturn(Arrays.asList(fullRule));

        Map<String, Object> result = bookingService.getRefundPreview("FUTURE");

        assertTrue((Boolean) result.get("success"));
        assertEquals(100.0, (Double) result.get("refundAmount"), 0.01);
        assertEquals("100%", result.get("refundRate"));
    }

    // ==================== BS-18 退款预览 - 2-24小时部分退款 ====================
    @Test
    void testGetRefundPreviewPartialRefund() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBookingCode("SOON");
        booking.setStatus("confirmed");
        booking.setShowtimeId(1L);
        booking.setTotalPrice(100.0);

        Showtime soonShowtime = new Showtime();
        soonShowtime.setShowDate(LocalDateTime.now().plusHours(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        soonShowtime.setShowTime(LocalDateTime.now().plusHours(5).format(DateTimeFormatter.ofPattern("HH:mm")));

        when(bookingRepository.findByBookingCode("SOON")).thenReturn(booking);
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(soonShowtime));
        RefundRule fullRule = new RefundRule();
        fullRule.setHoursBeforeShow(24);
        fullRule.setRefundRate(1.0);
        RefundRule partialRule = new RefundRule();
        partialRule.setHoursBeforeShow(2);
        partialRule.setRefundRate(0.8);
        when(refundRuleRepository.findAll()).thenReturn(Arrays.asList(fullRule, partialRule));

        Map<String, Object> result = bookingService.getRefundPreview("SOON");

        assertTrue((Boolean) result.get("success"));
        assertEquals(80.0, (Double) result.get("refundAmount"), 0.01);
    }

    // ==================== BS-20 退款预览 - 已取消订单 ====================
    @Test
    void testGetRefundPreviewAlreadyCancelled() {
        Booking booking = new Booking();
        booking.setBookingCode("CANCELLED");
        booking.setStatus("cancelled");
        when(bookingRepository.findByBookingCode("CANCELLED")).thenReturn(booking);

        Map<String, Object> result = bookingService.getRefundPreview("CANCELLED");

        assertFalse((Boolean) result.get("success"));
        assertEquals("订单已取消", result.get("message"));
    }

    // ==================== BS-21 改签 - 成功 ====================
    @Test
    void testChangeTicketSuccess() {
        Booking original = new Booking();
        original.setId(1L);
        original.setBookingCode("OLD123");
        original.setUserId(1L);
        original.setUserName("张三");
        original.setUserPhone("13800000000");
        original.setStatus("confirmed");
        original.setShowtimeId(1L);

        Showtime newShowtime = new Showtime();
        newShowtime.setId(2L);
        newShowtime.setMovieId(1L);
        newShowtime.setHallName("2号厅");
        newShowtime.setShowDate(LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        newShowtime.setShowTime("19:00");
        newShowtime.setPriceStandard(39.9);
        newShowtime.setPriceVip(59.9);

        when(bookingRepository.findByBookingCode("OLD123")).thenReturn(original);
        when(showtimeRepository.findById(2L)).thenReturn(Optional.of(newShowtime));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        RefundRule rule = new RefundRule();
        rule.setHoursBeforeShow(2);
        rule.setRefundRate(1.0);
        when(refundRuleRepository.findAll()).thenReturn(Arrays.asList(rule));
        when(seatRepository.findById(102L)).thenReturn(Optional.of(seat2));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = bookingService.changeTicket("OLD123", 2L, Arrays.asList(102L));

        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("newBookingCode"));
        assertNotNull(result.get("refundAmount"));
        assertEquals("cancelled", original.getStatus());
    }

    // ==================== BS-22 改签 - 原订单不存在 ====================
    @Test
    void testChangeTicketOriginalNotFound() {
        when(bookingRepository.findByBookingCode("NONE")).thenReturn(null);

        Map<String, Object> result = bookingService.changeTicket("NONE", 2L, Arrays.asList(102L));

        assertFalse((Boolean) result.get("success"));
        assertEquals("原订单不存在", result.get("message"));
    }

    // ==================== BS-24 支付 - 成功 ====================
    @Test
    void testProcessPaymentSuccess() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBookingCode("PAY123");
        booking.setShowtimeId(1L);
        booking.setTotalPrice(79.8);
        booking.setUserId(1L);
        booking.setStatus("confirmed");
        booking.setPaymentStatus("unpaid");

        when(bookingRepository.findByBookingCode("PAY123")).thenReturn(booking);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        Map<String, Object> result = bookingService.processPayment("PAY123", "wechat");

        assertTrue((Boolean) result.get("success"));
        assertEquals("paid", booking.getPaymentStatus());
        assertEquals(79.8, (Double) result.get("actualPaid"), 0.01);
        assertEquals("wechat", booking.getPaymentMethod());
        verify(userRepository, times(1)).save(any(User.class));
    }

    // ==================== BS-25 支付 - 使用优惠券 ====================
    @Test
    void testProcessPaymentWithCoupon() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBookingCode("COUPON123");
        booking.setTotalPrice(100.0);
        booking.setCouponId(1L);
        booking.setActualPaid(90.0);
        booking.setUserId(1L);
        booking.setStatus("confirmed");

        UserCoupon uc = new UserCoupon();
        uc.setId(1L);
        uc.setUserId(1L);
        uc.setCouponId(1L);
        uc.setStatus("unused");

        when(bookingRepository.findByBookingCode("COUPON123")).thenReturn(booking);
        when(userCouponRepository.findByUserIdAndCouponId(1L, 1L)).thenReturn(uc);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userCouponRepository.save(any(UserCoupon.class))).thenReturn(uc);
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        Map<String, Object> result = bookingService.processPayment("COUPON123", "wechat");

        assertTrue((Boolean) result.get("success"));
        assertEquals("used", uc.getStatus());
        assertNotNull(uc.getUsedAt());
        verify(userCouponRepository, times(1)).save(any(UserCoupon.class));
    }

    // ==================== BS-26 支付 - 订单不存在 ====================
    @Test
    void testProcessPaymentBookingNotFound() {
        when(bookingRepository.findByBookingCode("INVALID")).thenReturn(null);

        Map<String, Object> result = bookingService.processPayment("INVALID", "wechat");

        assertFalse((Boolean) result.get("success"));
        assertEquals("订单不存在", result.get("message"));
    }

    // ==================== BS-27 支付 - 积分与消费金额累加 ====================
    @Test
    void testProcessPaymentUpdatesPointsAndTotalSpent() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBookingCode("POINTS123");
        booking.setTotalPrice(100.0);
        booking.setUserId(1L);
        booking.setStatus("confirmed");

        User localUser = new User();
        localUser.setId(1L);
        localUser.setPoints(100);
        localUser.setTotalSpent(200.0);
        localUser.setMemberLevel(1);

        when(bookingRepository.findByBookingCode("POINTS123")).thenReturn(booking);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(userRepository.findById(1L)).thenReturn(Optional.of(localUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        bookingService.processPayment("POINTS123", "wechat");

        assertEquals(200, localUser.getPoints());
        assertEquals(300.0, localUser.getTotalSpent(), 0.01);
        assertTrue(localUser.getMemberLevel() >= 1);
    }

    // ==================== BS-28 所有订单 - 管理员视图 ====================
    @Test
    void testGetAllBookingsForAdmin() {
        Booking b1 = new Booking();
        b1.setId(1L);
        b1.setMovieTitle("电影A");
        b1.setStatus("confirmed");
        b1.setCreatedAt("2026-06-12 10:00:00");

        Booking b2 = new Booking();
        b2.setId(2L);
        b2.setMovieTitle("电影B");
        b2.setStatus("confirmed");
        b2.setCreatedAt("2026-06-12 11:00:00");

        when(bookingRepository.findAll()).thenReturn(Arrays.asList(b1, b2));
        when(bookingSeatRepository.findByBookingId(anyLong())).thenReturn(new ArrayList<>());

        Map<String, Object> result = bookingService.getAllBookings();

        assertTrue((Boolean) result.get("success"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("data");
        assertEquals(2, list.size());
        // 按时间倒序：b2 在先
        assertEquals("电影B", list.get(0).get("movieTitle"));
    }
}
