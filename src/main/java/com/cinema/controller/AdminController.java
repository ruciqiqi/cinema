package com.cinema.controller;

import com.cinema.entity.*;
import com.cinema.repository.*;
import com.cinema.service.MovieService;
import com.cinema.service.SnackService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final MovieService movieService;
    private final SnackService snackService;
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
    private final CinemaRepository cinemaRepository;

    public AdminController(MovieService movieService, SnackService snackService,
                           MovieRepository movieRepository, ShowtimeRepository showtimeRepository,
                           BookingRepository bookingRepository, HallRepository hallRepository,
                           BookingSeatRepository bookingSeatRepository, UserRepository userRepository,
                           CouponRepository couponRepository, UserCouponRepository userCouponRepository,
                           ReviewRepository reviewRepository, AnnouncementRepository announcementRepository,
                           CinemaRepository cinemaRepository) {
        this.movieService = movieService;
        this.snackService = snackService;
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
        this.cinemaRepository = cinemaRepository;
    }

    // ===== Dashboard stats =====
    @GetMapping("/stats")
    public Map<String, Object> stats() {
        Map<String, Object> result = new HashMap<>();
        result.put("movieCount", movieRepository.count());
        result.put("showtimeCount", showtimeRepository.count());
        result.put("bookingCount", bookingRepository.count());
        result.put("hallCount", hallRepository.count());
        result.put("userCount", userRepository.count());
        // Revenue
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

    // ===== Movies =====
    @GetMapping("/movies")
    public Map<String, Object> listMovies() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", movieService.getAllMovies());
        return result;
    }

    @PostMapping("/movies")
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
    public Map<String, Object> deleteMovie(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        movieRepository.deleteById(id);
        result.put("success", true);
        return result;
    }

    // ===== Showtimes =====
    @GetMapping("/showtimes")
    public Map<String, Object> listShowtimes() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", showtimeRepository.findAll());
        return result;
    }

    @PostMapping("/showtimes")
    public Map<String, Object> addShowtime(@RequestBody Showtime showtime) {
        Map<String, Object> result = new HashMap<>();
        showtime.setId(null);
        // Set hall name from hall
        Hall hall = hallRepository.findById(showtime.getHallId()).orElse(null);
        if (hall != null) showtime.setHallName(hall.getName());
        showtimeRepository.save(showtime);
        result.put("success", true);
        result.put("data", showtime);
        return result;
    }

    @DeleteMapping("/showtimes/{id}")
    public Map<String, Object> deleteShowtime(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        showtimeRepository.deleteById(id);
        result.put("success", true);
        return result;
    }

    // ===== Bookings =====
    @GetMapping("/bookings")
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
    public Map<String, Object> cancelBooking(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        Booking b = bookingRepository.findById(id).orElse(null);
        if (b == null) {
            result.put("success", false);
            result.put("message", "订单不存在");
            return result;
        }
        b.setStatus("cancelled");
        bookingRepository.save(b);
        result.put("success", true);
        return result;
    }

    // ===== Snacks =====
    @GetMapping("/snacks")
    public Map<String, Object> listSnacks() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", snackService.getAllSnacks());
        return result;
    }

    @PostMapping("/snacks")
    public Map<String, Object> addSnack(@RequestBody Snack snack) {
        Map<String, Object> result = new HashMap<>();
        snack.setId(null);
        snackService.save(snack);
        result.put("success", true);
        return result;
    }

    @PutMapping("/snacks/{id}")
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
    public Map<String, Object> deleteSnack(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        snackService.delete(id);
        result.put("success", true);
        return result;
    }

    // ===== Halls =====
    @GetMapping("/halls")
    public Map<String, Object> listHalls() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", hallRepository.findAll());
        return result;
    }

    // ===== Users =====
    @GetMapping("/users")
    public Map<String, Object> listUsers() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", userRepository.findAll());
        return result;
    }

    @PutMapping("/users/{id}")
    public Map<String, Object> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            result.put("success", false); result.put("message", "用户不存在"); return result;
        }
        if (body.containsKey("role")) user.setRole((String) body.get("role"));
        if (body.containsKey("memberLevel")) user.setMemberLevel(Integer.valueOf(body.get("memberLevel").toString()));
        if (body.containsKey("points")) user.setPoints(Integer.valueOf(body.get("points").toString()));
        userRepository.save(user);
        result.put("success", true);
        return result;
    }

    // ===== Coupons =====
    @GetMapping("/coupons")
    public Map<String, Object> listCoupons() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", couponRepository.findAll());
        return result;
    }

    @PostMapping("/coupons")
    public Map<String, Object> addCoupon(@RequestBody Coupon coupon) {
        Map<String, Object> result = new HashMap<>();
        coupon.setId(null);
        coupon.setUsedCount(0);
        couponRepository.save(coupon);
        result.put("success", true);
        return result;
    }

    @DeleteMapping("/coupons/{id}")
    public Map<String, Object> deleteCoupon(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        couponRepository.deleteById(id);
        result.put("success", true);
        return result;
    }

    // ===== Announcements =====
    @GetMapping("/announcements")
    public Map<String, Object> listAnnouncements() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", announcementRepository.findAll());
        return result;
    }

    @PostMapping("/announcements")
    public Map<String, Object> addAnnouncement(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();
        Announcement a = new Announcement();
        a.setTitle((String) body.get("title"));
        a.setContent((String) body.get("content"));
        a.setCreatedAt(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        a.setStatus("published");
        announcementRepository.save(a);
        result.put("success", true);
        return result;
    }

    @DeleteMapping("/announcements/{id}")
    public Map<String, Object> deleteAnnouncement(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        announcementRepository.deleteById(id);
        result.put("success", true);
        return result;
    }

    // ===== Cinemas =====
    @GetMapping("/cinemas")
    public Map<String, Object> listCinemas() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", cinemaRepository.findAll());
        return result;
    }

    @PostMapping("/cinemas")
    public Map<String, Object> addCinema(@RequestBody Cinema cinema) {
        Map<String, Object> result = new HashMap<>();
        cinema.setId(null);
        cinemaRepository.save(cinema);
        result.put("success", true);
        return result;
    }

    // ===== Enhanced Stats =====
    @GetMapping("/stats/revenue")
    public Map<String, Object> revenueStats() {
        Map<String, Object> result = new HashMap<>();
        // Revenue by movie
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
    public Map<String, Object> fullStats() {
        Map<String, Object> result = new HashMap<>();
        result.put("movieCount", movieRepository.count());
        result.put("showtimeCount", showtimeRepository.count());
        result.put("bookingCount", bookingRepository.count());
        result.put("hallCount", hallRepository.count());
        result.put("userCount", userRepository.count());
        result.put("cinemaCount", cinemaRepository.count());

        // Revenue
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

        // Genre distribution
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

        // Top movies by booking count
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
