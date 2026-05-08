package com.cinema.config;

import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    private boolean isPublicPath(String path) {
        if (path.startsWith("/api/movies")) return true;
        if (path.startsWith("/api/showtimes")) return true;
        if (path.startsWith("/api/seats")) return true;
        if (path.startsWith("/api/bookings/query")) return true;
        if (path.startsWith("/api/bookings/refund-preview")) return true;
        if (path.startsWith("/api/auth/")) return true;
        if (path.startsWith("/api/snacks")) return true;
        if (path.startsWith("/api/cinemas")) return true;
        if (path.startsWith("/api/announcements")) return true;
        if (path.startsWith("/api/reviews/movie/")) return true;
        if (path.startsWith("/api/coupons/available")) return true;
        return false;
    }

    /** Try to extract JWT token if present — never blocks, just sets request attributes */
    private void tryExtractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                Claims claims = jwtUtil.parseToken(token);
                request.setAttribute("userId", claims.get("userId", Long.class));
                request.setAttribute("username", claims.get("username", String.class));
                request.setAttribute("role", claims.get("role", String.class));
            }
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Always try to extract token for logged-in users, even on public paths
        tryExtractToken(request);

        // Public read-only paths — no auth required
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        // GET to most endpoints — no auth required (except user/admin data)
        if ("GET".equals(method)
                && !path.startsWith("/api/admin")
                && !path.startsWith("/api/bookings/my")
                && !path.startsWith("/api/bookings/all")
                && !path.startsWith("/api/user/")
                && !path.startsWith("/api/coupons/my")
                && !path.startsWith("/api/coupons/apply")
                && !path.startsWith("/api/reviews/my")) {
            chain.doFilter(request, response);
            return;
        }

        // All mutating requests require login
        Object uid = request.getAttribute("userId");
        if (uid == null) {
            sendError(response, 401, "请先登录");
            return;
        }

        String role = (String) request.getAttribute("role");

        // Admin-only paths
        if (path.startsWith("/api/admin")) {
            if (!"admin".equals(role)) {
                sendError(response, 403, "需要管理员权限");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":false,\"message\":\"" + message + "\"}");
    }
}
