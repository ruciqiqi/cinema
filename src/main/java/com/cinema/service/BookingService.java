package com.cinema.service;

import com.cinema.entity.*;
import com.cinema.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final SnackRepository snackRepository;
    private final RefundRuleRepository refundRuleRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final SeatService seatService;

    public BookingService(BookingRepository bookingRepository, BookingSeatRepository bookingSeatRepository,
                       SeatRepository seatRepository, ShowtimeRepository showtimeRepository,
                       MovieRepository movieRepository, SnackRepository snackRepository,
                       RefundRuleRepository refundRuleRepository,
                       UserRepository userRepository,
                       NotificationRepository notificationRepository,
                       SeatService seatService) {
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.seatRepository = seatRepository;
        this.showtimeRepository = showtimeRepository;
        this.movieRepository = movieRepository;
        this.snackRepository = snackRepository;
        this.refundRuleRepository = refundRuleRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.seatService = seatService;
    }

    @Transactional
    public Map<String, Object> createBooking(Long showtimeId, List<Long> seatIds,
                                              String userName, String userPhone,
                                              Long userId, String snacksJson) {
        Map<String, Object> result = new HashMap<>();

        Showtime showtime = showtimeRepository.findById(showtimeId).orElse(null);
        if (showtime == null) {
            result.put("success", false);
            result.put("message", "场次不存在");
            return result;
        }

        // Check if seats are already booked
        Set<Long> bookedSeats = seatService.getBookedSeatIds(showtimeId);
        for (Long seatId : seatIds) {
            if (bookedSeats.contains(seatId)) {
                result.put("success", false);
                result.put("message", "座位已被预订，请重新选择");
                return result;
            }
        }

        // Calculate total price
        double totalPrice = 0;
        List<String> seatLabels = new ArrayList<>();
        for (Long seatId : seatIds) {
            Seat seat = seatRepository.findById(seatId).orElse(null);
            if (seat == null) continue;
            String label = seat.getRowLabel() + "排" + seat.getSeatNum() + "座";
            seatLabels.add(label);
            if ("vip".equals(seat.getSeatType())) {
                totalPrice += showtime.getPriceVip();
            } else {
                totalPrice += showtime.getPriceStandard();
            }
        }
        totalPrice = Math.round(totalPrice * 100.0) / 100.0;

        // Add snack prices if any
        StringBuilder snacksSummary = new StringBuilder();
        if (snacksJson != null && !snacksJson.isEmpty()) {
            String[] parts = snacksJson.split(",");
            for (String part : parts) {
                String[] kv = part.split(":");
                if (kv.length == 2) {
                    try {
                        Long snackId = Long.valueOf(kv[0]);
                        int qty = Integer.parseInt(kv[1]);
                        Snack snack = snackRepository.findById(snackId).orElse(null);
                        if (snack != null && qty > 0) {
                            totalPrice += snack.getPrice() * qty;
                            if (snacksSummary.length() > 0) snacksSummary.append(", ");
                            snacksSummary.append(snack.getName()).append("x").append(qty);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
            totalPrice = Math.round(totalPrice * 100.0) / 100.0;
        }

        // Get movie info
        Movie movie = movieRepository.findById(showtime.getMovieId()).orElse(null);
        String movieTitle = movie != null ? movie.getTitle() : "";

        // Create booking
        String bookingCode = "CIN" + System.currentTimeMillis() % 100000000;
        Booking booking = new Booking();
        booking.setShowtimeId(showtimeId);
        booking.setUserName(userName);
        booking.setUserPhone(userPhone);
        booking.setTotalPrice(totalPrice);
        booking.setBookingCode(bookingCode);
        booking.setStatus("confirmed");
        booking.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        booking.setMovieTitle(movieTitle);
        booking.setHallName(showtime.getHallName());
        booking.setShowDate(showtime.getShowDate());
        booking.setShowTime(showtime.getShowTime());
        if (userId != null) booking.setUserId(userId);
        if (snacksSummary.length() > 0) booking.setSnacksJson(snacksSummary.toString());
        booking = bookingRepository.save(booking);

        // Save booking seats
        for (Long seatId : seatIds) {
            Seat seat = seatRepository.findById(seatId).orElse(null);
            if (seat == null) continue;
            BookingSeat bs = new BookingSeat();
            bs.setBookingId(booking.getId());
            bs.setSeatId(seatId);
            bs.setSeatLabel(seat.getRowLabel() + "排" + seat.getSeatNum() + "座");
            bookingSeatRepository.save(bs);
        }

        result.put("success", true);
        result.put("bookingCode", bookingCode);
        result.put("totalPrice", totalPrice);
        result.put("seatLabels", seatLabels);
        result.put("movieTitle", movieTitle);
        result.put("hallName", showtime.getHallName());
        result.put("showDate", showtime.getShowDate());
        result.put("showTime", showtime.getShowTime());
        return result;
    }

    public Map<String, Object> getBookingByCode(String bookingCode) {
        Map<String, Object> result = new HashMap<>();
        Booking booking = bookingRepository.findByBookingCode(bookingCode);
        if (booking == null) {
            result.put("success", false);
            result.put("message", "订单不存在");
            return result;
        }
        List<BookingSeat> bsList = bookingSeatRepository.findByBookingId(booking.getId());
        List<String> seatLabels = new ArrayList<>();
        for (BookingSeat bs : bsList) {
            seatLabels.add(bs.getSeatLabel());
        }
        result.put("success", true);
        result.put("booking", booking);
        result.put("seatLabels", seatLabels);
        return result;
    }

    public List<Map<String, Object>> getBookingsByUserId(Long userId) {
        List<Map<String, Object>> list = new ArrayList<>();
        List<Booking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
        for (Booking b : bookings) {
            list.add(buildBookingMap(b));
        }
        return list;
    }

    private Map<String, Object> buildBookingMap(Booking b) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", b.getId());
        m.put("bookingCode", b.getBookingCode());
        m.put("movieTitle", b.getMovieTitle());
        m.put("hallName", b.getHallName());
        m.put("showDate", b.getShowDate());
        m.put("showTime", b.getShowTime());
        m.put("userName", b.getUserName());
        m.put("userPhone", b.getUserPhone());
        m.put("totalPrice", b.getTotalPrice());
        m.put("status", b.getStatus());
        m.put("createdAt", b.getCreatedAt());
        m.put("snacksJson", b.getSnacksJson());
        List<BookingSeat> bsList = bookingSeatRepository.findByBookingId(b.getId());
        List<String> seatLabels = new ArrayList<>();
        for (BookingSeat bs : bsList) seatLabels.add(bs.getSeatLabel());
        m.put("seatLabels", seatLabels);
        return m;
    }

    @Transactional
    public Map<String, Object> cancelBooking(String bookingCode) {
        Map<String, Object> result = new HashMap<>();
        Booking booking = bookingRepository.findByBookingCode(bookingCode);
        if (booking == null) {
            result.put("success", false);
            result.put("message", "订单不存在");
            return result;
        }
        if ("cancelled".equals(booking.getStatus())) {
            result.put("success", false);
            result.put("message", "订单已取消，无法重复操作");
            return result;
        }
        double refundAmount = calculateRefund(booking);
        booking.setStatus("cancelled");
        bookingRepository.save(booking);
        result.put("success", true);
        result.put("message", "订单已取消");
        result.put("refundAmount", refundAmount);
        return result;
    }

    private double calculateRefund(Booking booking, Map<String, Object> detail) {
        Showtime st = showtimeRepository.findById(booking.getShowtimeId()).orElse(null);
        if (st == null) {
            if (detail != null) detail.put("reason", "场次不存在");
            return 0;
        }
        String showDateTime = st.getShowDate() + " " + st.getShowTime();
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        java.time.LocalDateTime showTime;
        try {
            showTime = java.time.LocalDateTime.parse(showDateTime, fmt);
        } catch (Exception e) {
            if (detail != null) detail.put("reason", "场次时间解析失败");
            return 0;
        }
        long hoursUntilShow = java.time.Duration.between(java.time.LocalDateTime.now(), showTime).toHours();

        List<RefundRule> rules = refundRuleRepository.findAll();
        if (rules.isEmpty()) {
            if (detail != null) detail.put("reason", "退票规则未配置");
            return 0;
        }
        rules.sort((a, b) -> b.getHoursBeforeShow() - a.getHoursBeforeShow());
        RefundRule matched = null;
        for (RefundRule rule : rules) {
            if (hoursUntilShow >= rule.getHoursBeforeShow()) {
                matched = rule;
                break;
            }
        }
        if (matched == null) {
            matched = rules.get(rules.size() - 1); // lowest rule
        }
        Double totalObj = booking.getActualPaid() != null ? booking.getActualPaid() : booking.getTotalPrice();
        double total = totalObj != null ? totalObj : 0;
        double refund = Math.round(total * matched.getRefundRate() * 100.0) / 100.0;

        if (detail != null) {
            detail.put("hoursUntilShow", hoursUntilShow);
            detail.put("appliedRule", matched.getDescription());
            detail.put("refundRate", (int)(matched.getRefundRate() * 100) + "%");
            detail.put("originalAmount", total);
        }
        return refund;
    }

    private double calculateRefund(Booking booking) {
        return calculateRefund(booking, null);
    }

    public Map<String, Object> getRefundPreview(String bookingCode) {
        Map<String, Object> result = new HashMap<>();
        Booking booking = bookingRepository.findByBookingCode(bookingCode);
        if (booking == null) {
            result.put("success", false); result.put("message", "订单不存在"); return result;
        }
        if ("cancelled".equals(booking.getStatus())) {
            result.put("success", false); result.put("message", "订单已取消"); return result;
        }
        Map<String, Object> detail = new HashMap<>();
        double refund = calculateRefund(booking, detail);
        result.put("success", true);
        result.put("bookingCode", bookingCode);
        result.put("originalAmount", detail.get("originalAmount"));
        result.put("refundAmount", refund);
        result.put("hoursUntilShow", detail.get("hoursUntilShow"));
        result.put("appliedRule", detail.get("appliedRule"));
        result.put("refundRate", detail.get("refundRate"));
        return result;
    }

    @Transactional
    public Map<String, Object> changeTicket(String bookingCode, Long newShowtimeId, List<Long> newSeatIds) {
        Map<String, Object> result = new HashMap<>();
        Booking original = bookingRepository.findByBookingCode(bookingCode);
        if (original == null) {
            result.put("success", false); result.put("message", "原订单不存在"); return result;
        }
        if ("cancelled".equals(original.getStatus())) {
            result.put("success", false); result.put("message", "原订单已取消"); return result;
        }

        Showtime newSt = showtimeRepository.findById(newShowtimeId).orElse(null);
        if (newSt == null) {
            result.put("success", false); result.put("message", "新场次不存在"); return result;
        }

        // Cancel original
        double refundAmount = calculateRefund(original);
        original.setStatus("cancelled");
        original.setIsChanged(true);
        bookingRepository.save(original);

        // Check new seats are available
        Set<Long> bookedSeats = getBookedSeatIdsStatic(newShowtimeId);
        for (Long seatId : newSeatIds) {
            if (bookedSeats.contains(seatId)) {
                result.put("success", false);
                result.put("message", "新场次座位已被预订"); return result;
            }
        }

        // Calculate new price
        double totalPrice = 0;
        List<String> seatLabels = new ArrayList<>();
        for (Long seatId : newSeatIds) {
            Seat seat = seatRepository.findById(seatId).orElse(null);
            if (seat == null) continue;
            String label = seat.getRowLabel() + "排" + seat.getSeatNum() + "座";
            seatLabels.add(label);
            totalPrice += "vip".equals(seat.getSeatType()) ? newSt.getPriceVip() : newSt.getPriceStandard();
        }
        totalPrice = Math.round(totalPrice * 100.0) / 100.0;

        Movie movie = movieRepository.findById(newSt.getMovieId()).orElse(null);
        String movieTitle = movie != null ? movie.getTitle() : "";
        String newBookingCode = "CIN" + System.currentTimeMillis() % 100000000;

        Booking newBooking = new Booking();
        newBooking.setShowtimeId(newShowtimeId);
        newBooking.setUserName(original.getUserName());
        newBooking.setUserPhone(original.getUserPhone());
        newBooking.setTotalPrice(totalPrice);
        newBooking.setActualPaid(totalPrice);
        newBooking.setBookingCode(newBookingCode);
        newBooking.setStatus("confirmed");
        newBooking.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        newBooking.setMovieTitle(movieTitle);
        newBooking.setHallName(newSt.getHallName());
        newBooking.setShowDate(newSt.getShowDate());
        newBooking.setShowTime(newSt.getShowTime());
        newBooking.setUserId(original.getUserId());
        newBooking.setOriginalBookingId(original.getId());
        newBooking = bookingRepository.save(newBooking);

        for (Long seatId : newSeatIds) {
            Seat seat = seatRepository.findById(seatId).orElse(null);
            if (seat == null) continue;
            BookingSeat bs = new BookingSeat();
            bs.setBookingId(newBooking.getId());
            bs.setSeatId(seatId);
            bs.setSeatLabel(seat.getRowLabel() + "排" + seat.getSeatNum() + "座");
            bookingSeatRepository.save(bs);
        }

        result.put("success", true);
        result.put("refundAmount", refundAmount);
        result.put("newBookingCode", newBookingCode);
        result.put("newTotalPrice", totalPrice);
        result.put("seatLabels", seatLabels);
        return result;
    }

    private Set<Long> getBookedSeatIdsStatic(Long showtimeId) {
        Set<Long> booked = new HashSet<>();
        List<Booking> bookings = bookingRepository.findConfirmedByShowtimeId(showtimeId);
        for (Booking b : bookings) {
            List<BookingSeat> bsList = bookingSeatRepository.findByBookingId(b.getId());
            for (BookingSeat bs : bsList) booked.add(bs.getSeatId());
        }
        return booked;
    }

    public Map<String, Object> processPayment(String bookingCode, String paymentMethod) {
        Map<String, Object> result = new HashMap<>();
        Booking booking = bookingRepository.findByBookingCode(bookingCode);
        if (booking == null) {
            result.put("success", false); result.put("message", "订单不存在"); return result;
        }
        booking.setPaymentMethod(paymentMethod != null ? paymentMethod : "wechat");
        booking.setPaymentStatus("paid");
        double actualPaid = booking.getActualPaid() != null ? booking.getActualPaid() : booking.getTotalPrice();
        booking.setActualPaid(actualPaid);
        booking.setTicketQrCode("CINEMA:" + bookingCode + ":" + System.currentTimeMillis());
        bookingRepository.save(booking);

        // Award points (1 yuan = 1 point)
        if (booking.getUserId() != null) {
            userRepository.findById(booking.getUserId()).ifPresent(user -> {
                user.setPoints(user.getPoints() + (int) actualPaid);
                user.setTotalSpent(user.getTotalSpent() + actualPaid);
                // Update member level based on total spent
                double ts = user.getTotalSpent();
                if (ts >= 5000) user.setMemberLevel(5);
                else if (ts >= 2000) user.setMemberLevel(4);
                else if (ts >= 1000) user.setMemberLevel(3);
                else if (ts >= 500) user.setMemberLevel(2);
                else if (ts >= 200) user.setMemberLevel(1);
                userRepository.save(user);
            });
        }

        // Create notification for the user
        if (booking.getUserId() != null) {
            Notification notif = new Notification();
            notif.setUserId(booking.getUserId());
            notif.setTitle("购票成功");
            notif.setContent("您已成功购买《" + (booking.getMovieTitle() != null ? booking.getMovieTitle() : "") + "》" + (booking.getShowDate() != null ? booking.getShowDate() : "") + " " + (booking.getShowTime() != null ? booking.getShowTime() : "") + " 电影票");
            notif.setActionUrl("/my/orders");
            notif.setIsRead(false);
            notif.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            notificationRepository.save(notif);
        }

        result.put("success", true);
        result.put("message", "支付成功");
        result.put("bookingCode", bookingCode);
        result.put("qrCode", booking.getTicketQrCode());
        result.put("actualPaid", actualPaid);
        return result;
    }

    public Map<String, Object> getAllBookings() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Booking b : bookingRepository.findAll()) {
            Map<String, Object> m = buildBookingMap(b);
            list.add(m);
        }
        list.sort((a, b) -> {
            String ta = (String) a.getOrDefault("createdAt", "");
            String tb = (String) b.getOrDefault("createdAt", "");
            return tb.compareTo(ta);
        });
        result.put("success", true);
        result.put("data", list);
        return result;
    }
}
