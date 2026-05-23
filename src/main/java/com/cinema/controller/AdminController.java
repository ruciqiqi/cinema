package com.cinema.controller;

import com.cinema.dto.request.AddAnnouncementRequest;
import com.cinema.dto.request.UpdateUserRequest;
import com.cinema.entity.*;
import com.cinema.repository.*;
import com.cinema.service.BookingService;
import com.cinema.service.MovieService;
import com.cinema.service.ShowtimeService;
import com.cinema.service.SnackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理员管理", description = "后台管理功能接口")
public class AdminController {
    private final MovieService movieService;
    private final SnackService snackService;
    private final ShowtimeService showtimeService;
    private final BookingService bookingService;
    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;
    private final BookingRepository bookingRepository;
    private final HallRepository hallRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final ReviewRepository reviewRepository;
    private final AnnouncementRepository announcementRepository;

    public AdminController(MovieService movieService, SnackService snackService,
                           ShowtimeService showtimeService, BookingService bookingService,
                           MovieRepository movieRepository, ShowtimeRepository showtimeRepository,
                           BookingRepository bookingRepository, HallRepository hallRepository,
                           BookingSeatRepository bookingSeatRepository, UserRepository userRepository,
                           CouponRepository couponRepository, UserCouponRepository userCouponRepository,
                           ReviewRepository reviewRepository, AnnouncementRepository announcementRepository) {
        this.movieService = movieService;
        this.snackService = snackService;
        this.showtimeService = showtimeService;
        this.bookingService = bookingService;
        this.movieRepository = movieRepository;
        this.showtimeRepository = showtimeRepository;
        this.bookingRepository = bookingRepository;
        this.hallRepository = hallRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.userRepository = userRepository;
        this.couponRepository = couponRepository;
        this.userCouponRepository = userCouponRepository;
        this.reviewRepository = reviewRepository;
        this.announcementRepository = announcementRepository;
    }

    @GetMapping("/stats")
    @Operation(summary = "获取统计数据", description = "获取系统统计信息")
    public Map<String, Object> stats() {
        Map<String, Object> result = new HashMap<>();
        result.put("movieCount", movieRepository.count());
        result.put("showtimeCount", showtimeRepository.count());
        result.put("bookingCount", bookingRepository.count());
        result.put("hallCount", hallRepository.count());
        result.put("userCount", userRepository.count());
        double revenue = 0;
        for (Booking b : bookingRepository.findAll()) {
            if ("confirmed".equals(b.getStatus())) {
                revenue += b.getTotalPrice() != null ? b.getTotalPrice() : 0;
            }
        }
        result.put("revenue", Math.round(revenue * 100.0) / 100.0);
        result.put("success", true);
        return result;
    }

    @GetMapping("/movies")
    @Operation(summary = "获取电影列表", description = "获取所有电影")
    public Map<String, Object> listMovies() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", movieService.getAllMovies());
        return result;
    }

    @PostMapping("/movies")
    @Operation(summary = "添加电影", description = "添加新电影")
    public Map<String, Object> addMovie(@RequestBody Movie movie) {
        Map<String, Object> result = new HashMap<>();
        movie.setId(null);
        if (movie.getStatus() == null) movie.setStatus("showing");
        movieRepository.save(movie);
        result.put("success", true);
        result.put("data", movie);
        return result;
    }

    @PutMapping("/movies/{id}")
    @Operation(summary = "更新电影", description = "更新电影信息")
    public Map<String, Object> updateMovie(@PathVariable Long id, @RequestBody Movie movie) {
        Map<String, Object> result = new HashMap<>();
        Movie exist = movieRepository.findById(id).orElse(null);
        if (exist == null) {
            result.put("success", false);
            result.put("message", "影片不存在");
            return result;
        }
        exist.setTitle(movie.getTitle());
        exist.setDirector(movie.getDirector());
        exist.setCast(movie.getCast());
        exist.setGenre(movie.getGenre());
        exist.setDuration(movie.getDuration());
        exist.setLanguage(movie.getLanguage());
        exist.setReleaseDate(movie.getReleaseDate());
        exist.setDescription(movie.getDescription());
        exist.setPosterBg(movie.getPosterBg());
        exist.setRating(movie.getRating());
        exist.setStatus(movie.getStatus());
        movieRepository.save(exist);
        result.put("success", true);
        return result;
    }

    @DeleteMapping("/movies/{id}")
    @Operation(summary = "删除电影", description = "删除指定电影")
    public Map<String, Object> deleteMovie(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        movieRepository.deleteById(id);
        result.put("success", true);
        return result;
    }

    @GetMapping("/showtimes")
    @Operation(summary = "获取场次列表", description = "获取所有场次")
    public Map<String, Object> listShowtimes() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", showtimeRepository.findAll());
        return result;
    }

    @PostMapping("/showtimes")
    @Operation(summary = "添加场次", description = "添加新场次")
    public Map<String, Object> addShowtime(@RequestBody Showtime showtime) {
        Map<String, Object> result = new HashMap<>();
        showtime.setId(null);
        Hall hall = hallRepository.findById(showtime.getHallId()).orElse(null);
        if (hall != null) showtime.setHallName(hall.getName());
        showtimeRepository.save(showtime);
        result.put("success", true);
        result.put("data", showtime);
        return result;
    }

    @DeleteMapping("/showtimes/{id}")
    @Operation(summary = "删除场次", description = "删除指定场次")
    public Map<String, Object> deleteShowtime(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        showtimeRepository.deleteById(id);
        result.put("success", true);
        return result;
    }

    @PostMapping("/showtimes/refresh")
    @Operation(summary = "刷新场次日期", description = "删除过期场次并生成新的3天滚动窗口")
    public Map<String, Object> refreshShowtimes() {
        Map<String, Object> result = new HashMap<>();
        int changes = showtimeService.maintainRollingShowtimes();
        result.put("success", true);
        result.put("changes", changes);
        result.put("message", "场次日期已刷新，共" + changes + "条变更");
        return result;
    }

    @GetMapping("/bookings")
    @Operation(summary = "获取订单列表", description = "获取所有订单")
    public Map<String, Object> listBookings() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Booking b : bookingRepository.findAll()) {
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
            List<BookingSeat> bsList = bookingSeatRepository.findByBookingId(b.getId());
            List<String> seatLabels = new ArrayList<>();
            for (BookingSeat bs : bsList) seatLabels.add(bs.getSeatLabel());
            m.put("seatLabels", seatLabels);
            list.add(m);
        }
        result.put("success", true);
        result.put("data", list);
        return result;
    }

    @PutMapping("/bookings/{id}/cancel")
    @Operation(summary = "取消订单", description = "管理员取消订单")
    public Map<String, Object> cancelBooking(@PathVariable Long id) {
        Booking b = bookingRepository.findById(id).orElse(null);
        if (b == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "订单不存在");
            return result;
        }
        return bookingService.cancelBooking(b.getBookingCode());
    }

    @GetMapping("/snacks")
    @Operation(summary = "获取零食列表", description = "获取所有零食")
    public Map<String, Object> listSnacks() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", snackService.getAllSnacks());
        return result;
    }

    @PostMapping("/snacks")
    @Operation(summary = "添加零食", description = "添加新零食")
    public Map<String, Object> addSnack(@RequestBody Snack snack) {
        Map<String, Object> result = new HashMap<>();
        snack.setId(null);
        snackService.save(snack);
        result.put("success", true);
        return result;
    }

    @PutMapping("/snacks/{id}")
    @Operation(summary = "更新零食", description = "更新零食信息")
    public Map<String, Object> updateSnack(@PathVariable Long id, @RequestBody Snack snack) {
        Map<String, Object> result = new HashMap<>();
        Snack exist = snackService.getById(id);
        if (exist == null) {
            result.put("success", false);
            result.put("message", "商品不存在");
            return result;
        }
        exist.setName(snack.getName());
        exist.setCategory(snack.getCategory());
        exist.setPrice(snack.getPrice());
        exist.setImage(snack.getImage());
        exist.setStatus(snack.getStatus());
        snackService.save(exist);
        result.put("success", true);
        return result;
    }

    @DeleteMapping("/snacks/{id}")
    @Operation(summary = "删除零食", description = "删除指定零食")
    public Map<String, Object> deleteSnack(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        snackService.delete(id);
        result.put("success", true);
        return result;
    }

    @GetMapping("/halls")
    @Operation(summary = "获取影厅列表", description = "获取所有影厅")
    public Map<String, Object> listHalls() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", hallRepository.findAll());
        return result;
    }

    @GetMapping("/users")
    @Operation(summary = "获取用户列表", description = "获取所有用户")
    public Map<String, Object> listUsers() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", userRepository.findAll());
        return result;
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "更新用户", description = "管理员更新用户信息")
    public Map<String, Object> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        Map<String, Object> result = new HashMap<>();
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            result.put("success", false); result.put("message", "用户不存在"); return result;
        }
        if (request.getRole() != null) user.setRole(request.getRole());
        if (request.getMemberLevel() != null) user.setMemberLevel(request.getMemberLevel());
        if (request.getPoints() != null) user.setPoints(request.getPoints());
        userRepository.save(user);
        result.put("success", true);
        return result;
    }

    @GetMapping("/coupons")
    @Operation(summary = "获取优惠券列表", description = "获取所有优惠券")
    public Map<String, Object> listCoupons() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", couponRepository.findAll());
        return result;
    }

    @PostMapping("/coupons")
    @Operation(summary = "添加优惠券", description = "添加新优惠券")
    public Map<String, Object> addCoupon(@RequestBody Coupon coupon) {
        Map<String, Object> result = new HashMap<>();
        coupon.setId(null);
        coupon.setUsedCount(0);
        couponRepository.save(coupon);
        result.put("success", true);
        return result;
    }

    @DeleteMapping("/coupons/{id}")
    @Operation(summary = "删除优惠券", description = "删除指定优惠券")
    public Map<String, Object> deleteCoupon(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        couponRepository.deleteById(id);
        result.put("success", true);
        return result;
    }

    @GetMapping("/announcements")
    @Operation(summary = "获取公告列表", description = "获取所有公告")
    public Map<String, Object> listAnnouncements() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", announcementRepository.findAll());
        return result;
    }

    @PostMapping("/announcements")
    @Operation(summary = "添加公告", description = "添加新公告")
    public Map<String, Object> addAnnouncement(@RequestBody AddAnnouncementRequest request) {
        Map<String, Object> result = new HashMap<>();
        Announcement a = new Announcement();
        a.setTitle(request.getTitle());
        a.setContent(request.getContent());
        a.setCreatedAt(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        a.setStatus("published");
        announcementRepository.save(a);
        result.put("success", true);
        return result;
    }

    @DeleteMapping("/announcements/{id}")
    @Operation(summary = "删除公告", description = "删除指定公告")
    public Map<String, Object> deleteAnnouncement(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        announcementRepository.deleteById(id);
        result.put("success", true);
        return result;
    }

    @GetMapping("/stats/revenue")
    @Operation(summary = "收入统计", description = "获取收入统计数据")
    public Map<String, Object> revenueStats() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Double> byMovie = new LinkedHashMap<>();
        Map<String, Double> byDate = new LinkedHashMap<>();
        for (Booking b : bookingRepository.findAll()) {
            if ("confirmed".equals(b.getStatus())) {
                double amt = b.getActualPaid() != null ? b.getActualPaid() : (b.getTotalPrice() != null ? b.getTotalPrice() : 0);
                String title = b.getMovieTitle() != null ? b.getMovieTitle() : "未知";
                byMovie.merge(title, amt, Double::sum);
                if (b.getShowDate() != null) {
                    byDate.merge(b.getShowDate(), amt, Double::sum);
                }
            }
        }
        result.put("byMovie", byMovie);
        result.put("byDate", byDate);
        result.put("success", true);
        return result;
    }

    @GetMapping("/stats/overview")
    @Operation(summary = "完整统计", description = "获取完整统计信息")
    public Map<String, Object> fullStats() {
        Map<String, Object> result = new HashMap<>();
        result.put("movieCount", movieRepository.count());
        result.put("showtimeCount", showtimeRepository.count());
        result.put("bookingCount", bookingRepository.count());
        result.put("hallCount", hallRepository.count());
        result.put("userCount", userRepository.count());

        double revenue = 0;
        int confirmedCount = 0;
        for (Booking b : bookingRepository.findAll()) {
            if ("confirmed".equals(b.getStatus())) {
                revenue += b.getActualPaid() != null ? b.getActualPaid() : (b.getTotalPrice() != null ? b.getTotalPrice() : 0);
                confirmedCount++;
            }
        }
        result.put("revenue", Math.round(revenue * 100.0) / 100.0);
        result.put("confirmedBookings", confirmedCount);

        Map<String, Integer> genreCount = new LinkedHashMap<>();
        for (Movie m : movieRepository.findAll()) {
            if (m.getGenre() != null) {
                String[] genres = m.getGenre().split("/");
                for (String g : genres) {
                    genreCount.merge(g.trim(), 1, Integer::sum);
                }
            }
        }
        result.put("genreDistribution", genreCount);

        Map<String, Integer> movieBookingCount = new LinkedHashMap<>();
        for (Booking b : bookingRepository.findAll()) {
            if ("confirmed".equals(b.getStatus()) && b.getMovieTitle() != null) {
                movieBookingCount.merge(b.getMovieTitle(), 1, Integer::sum);
            }
        }
        result.put("topMovies", movieBookingCount);

        result.put("success", true);
        return result;
    }
}