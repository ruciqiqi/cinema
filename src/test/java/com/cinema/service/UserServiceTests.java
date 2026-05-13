package com.cinema.service;

import com.cinema.config.JwtUtil;
import com.cinema.entity.User;
import com.cinema.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.MessageDigest;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private String sha256(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return password;
        }
    }

    @Test
    void testRegisterUserSuccess() {
        when(userRepository.findByUsername("testuser")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        doReturn("test-token").when(jwtUtil).generateToken(anyLong(), anyString(), anyString());

        Map<String, Object> result = userService.register("testuser", "password123", "12345678900");

        assertTrue((Boolean) result.get("success"));
        assertEquals("test-token", result.get("token"));
        assertEquals("testuser", result.get("username"));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUserExists() {
        User existingUser = new User();
        existingUser.setUsername("existinguser");
        when(userRepository.findByUsername("existinguser")).thenReturn(existingUser);

        Map<String, Object> result = userService.register("existinguser", "password123", "12345678900");

        assertFalse((Boolean) result.get("success"));
        assertEquals("用户名已存在", result.get("message"));
    }

    @Test
    void testRegisterUsernameTooShort() {
        Map<String, Object> result = userService.register("ab", "password123", "12345678900");

        assertFalse((Boolean) result.get("success"));
        assertEquals("用户名至少3个字符", result.get("message"));
    }

    @Test
    void testRegisterPasswordTooShort() {
        Map<String, Object> result = userService.register("testuser", "abc", "12345678900");

        assertFalse((Boolean) result.get("success"));
        assertEquals("密码至少6个字符", result.get("message"));
    }

    @Test
    void testLoginSuccess() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword(sha256("password"));
        user.setRole("user");

        when(userRepository.findByUsername("testuser")).thenReturn(user);
        doReturn("test-token").when(jwtUtil).generateToken(anyLong(), anyString(), anyString());

        Map<String, Object> result = userService.login("testuser", "password");

        assertTrue((Boolean) result.get("success"));
        assertEquals("test-token", result.get("token"));
        assertEquals("testuser", result.get("username"));
    }

    @Test
    void testLoginUserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(null);

        Map<String, Object> result = userService.login("nonexistent", "password");

        assertFalse((Boolean) result.get("success"));
        assertEquals("用户名或密码错误", result.get("message"));
    }

    @Test
    void testLoginWrongPassword() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword(sha256("correctpassword"));

        when(userRepository.findByUsername("testuser")).thenReturn(user);

        Map<String, Object> result = userService.login("testuser", "wrongpassword");

        assertFalse((Boolean) result.get("success"));
        assertEquals("用户名或密码错误", result.get("message"));
    }
}