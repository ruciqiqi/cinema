package com.cinema.service;

import com.cinema.config.JwtUtil;
import com.cinema.entity.Booking;
import com.cinema.entity.Coupon;
import com.cinema.entity.User;
import com.cinema.entity.UserCoupon;
import com.cinema.repository.BookingRepository;
import com.cinema.repository.CouponRepository;
import com.cinema.repository.UserCouponRepository;
import com.cinema.repository.UserRepository;
import com.cinema.util.PasswordUtil;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, BookingRepository bookingRepository,
                       CouponRepository couponRepository, UserCouponRepository userCouponRepository,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.couponRepository = couponRepository;
        this.userCouponRepository = userCouponRepository;
        this.jwtUtil = jwtUtil;
    }

    private void syncTotalSpent(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;
        double total = 0;
        for (Booking b : bookingRepository.findByUserId(userId)) {
            if (!"cancelled".equals(b.getStatus())) {
                total += b.getActualPaid() != null ? b.getActualPaid()
                        : (b.getTotalPrice() != null ? b.getTotalPrice() : 0);
            }
        }
        user.setTotalSpent(total);
        double ts = user.getTotalSpent();
        if (ts >= 5000) user.setMemberLevel(5);
        else if (ts >= 2000) user.setMemberLevel(4);
        else if (ts >= 1000) user.setMemberLevel(3);
        else if (ts >= 500) user.setMemberLevel(2);
        else user.setMemberLevel(1);
        userRepository.save(user);
    }

    public Map<String, Object> register(String username, String password, String phone) {
        Map<String, Object> result = new HashMap<>();
        if (username == null || username.trim().length() < 3) {
            result.put("success", false);
            result.put("message", "用户名至少3个字符");
            return result;
        }
        if (password == null || password.trim().length() < 6) {
            result.put("success", false);
            result.put("message", "密码至少6个字符");
            return result;
        }
        User exist = userRepository.findByUsername(username.trim());
        if (exist != null) {
            result.put("success", false);
            result.put("message", "用户名已存在");
            return result;
        }
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(hashPassword(password));
        user.setPhone(phone != null ? phone.trim() : "");
        user.setRole("user");
        user.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        result.put("success", true);
        result.put("token", token);
        result.put("username", user.getUsername());
        result.put("role", user.getRole());
        return result;
    }

    public Map<String, Object> login(String username, String password) {
        Map<String, Object> result = new HashMap<>();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }
        if (!PasswordUtil.matches(password, user.getPassword())) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        result.put("success", true);
        result.put("token", token);
        result.put("username", user.getUsername());
        result.put("role", user.getRole());
        result.put("phone", user.getPhone() != null ? user.getPhone() : "");
        return result;
    }

    public Map<String, Object> getProfile(Long userId) {
        Map<String, Object> result = new HashMap<>();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            result.put("success", false); result.put("message", "用户不存在"); return result;
        }
        syncTotalSpent(userId);
        user = userRepository.findById(userId).orElse(null);
        result.put("success", true);
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("phone", user.getPhone());
        profile.put("email", user.getEmail());
        profile.put("avatar", user.getAvatar());
        profile.put("gender", user.getGender());
        profile.put("birthday", user.getBirthday());
        profile.put("realName", user.getRealName());
        profile.put("idCard", user.getIdCard());
        profile.put("memberLevel", user.getMemberLevel());
        profile.put("points", user.getPoints());
        profile.put("totalSpent", user.getTotalSpent());
        profile.put("role", user.getRole());
        profile.put("createdAt", user.getCreatedAt());
        result.put("data", profile);
        return result;
    }

    public Map<String, Object> updateProfile(Long userId, Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            result.put("success", false); result.put("message", "用户不存在"); return result;
        }
        if (body.containsKey("phone")) user.setPhone((String) body.get("phone"));
        if (body.containsKey("email")) user.setEmail((String) body.get("email"));
        if (body.containsKey("gender")) user.setGender((String) body.get("gender"));
        if (body.containsKey("birthday")) user.setBirthday((String) body.get("birthday"));
        if (body.containsKey("avatar")) user.setAvatar((String) body.get("avatar"));
        userRepository.save(user);
        result.put("success", true);
        result.put("data", user);
        return result;
    }

    public Map<String, Object> changePassword(Long userId, String oldPwd, String newPwd) {
        Map<String, Object> result = new HashMap<>();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            result.put("success", false); result.put("message", "用户不存在"); return result;
        }
        if (!PasswordUtil.matches(oldPwd, user.getPassword())) {
            result.put("success", false); result.put("message", "原密码错误"); return result;
        }
        if (newPwd == null || newPwd.length() < 6) {
            result.put("success", false); result.put("message", "新密码至少6个字符"); return result;
        }
        user.setPassword(hashPassword(newPwd));
        userRepository.save(user);
        result.put("success", true); result.put("message", "密码修改成功");
        return result;
    }

    public Map<String, Object> realNameAuth(Long userId, String realName, String idCard) {
        Map<String, Object> result = new HashMap<>();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            result.put("success", false); result.put("message", "用户不存在"); return result;
        }
        if (realName == null || realName.trim().isEmpty()) {
            result.put("success", false); result.put("message", "请输入真实姓名"); return result;
        }
        if (idCard == null || idCard.trim().isEmpty()) {
            result.put("success", false); result.put("message", "请输入身份证号"); return result;
        }
        user.setRealName(realName.trim());
        user.setIdCard(idCard.trim());
        userRepository.save(user);
        result.put("success", true); result.put("message", "实名认证成功");
        return result;
    }

    public Map<String, Object> getPoints(Long userId) {
        Map<String, Object> result = new HashMap<>();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            result.put("success", false); result.put("message", "用户不存在"); return result;
        }
        syncTotalSpent(userId);
        user = userRepository.findById(userId).orElse(null);
        result.put("success", true);
        result.put("points", user.getPoints());
        result.put("memberLevel", user.getMemberLevel());
        result.put("totalSpent", user.getTotalSpent());
        return result;
    }

    public Map<String, Object> redeemPoints(Long userId, Integer points) {
        Map<String, Object> result = new HashMap<>();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            result.put("success", false); result.put("message", "用户不存在"); return result;
        }
        if (points == null || points <= 0) {
            result.put("success", false); result.put("message", "兑换积分数量无效"); return result;
        }
        if (user.getPoints() < points) {
            result.put("success", false); result.put("message", "积分不足"); return result;
        }
        user.setPoints(user.getPoints() - points);
        userRepository.save(user);
        double cash = points / 10.0;
        String today = LocalDate.now().toString();
        String expireDate = LocalDate.now().plusDays(30).toString();

        Coupon coupon = new Coupon();
        coupon.setCode("POINTS" + System.currentTimeMillis());
        coupon.setName("积分兑换" + (int) cash + "元代金券");
        coupon.setType("cash");
        coupon.setValue(cash);
        coupon.setMinAmount(0.0);
        coupon.setStartDate(today);
        coupon.setEndDate(expireDate);
        coupon.setUsageLimit(1);
        coupon.setUsedCount(0);
        coupon.setStatus("active");
        coupon = couponRepository.save(coupon);

        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setCouponId(coupon.getId());
        uc.setStatus("unused");
        uc.setReceivedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        userCouponRepository.save(uc);

        result.put("success", true);
        result.put("message", "已兑换 " + cash + " 元代金券，可在付款时使用");
        result.put("cash", cash);
        result.put("remainingPoints", user.getPoints());
        return result;
    }

    private String hashPassword(String password) {
        return PasswordUtil.encode(password);
    }
}
