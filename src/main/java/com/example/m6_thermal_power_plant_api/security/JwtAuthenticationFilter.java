package com.example.m6_thermal_power_plant_api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Nếu request không có header Authorization hoặc không bắt đầu bằng "Bearer ", bỏ qua và đi tiếp
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Cắt lấy chuỗi token đằng sau chữ "Bearer "
        String jwt = authHeader.substring(7);
        try {
            // Parse + verify (chữ ký, expiration) chỉ một lần
            Claims claims = jwtUtils.parse(jwt);
            String username = claims.getSubject();

            // Nếu có username và chưa được xác thực trong Context
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                List<String> roles = claims.get("roles", List.class);
                if (roles == null) roles = Collections.emptyList();
                Number accountIdNum = claims.get("accountId", Number.class);
                Integer accountId = accountIdNum != null ? accountIdNum.intValue() : null;

                CustomUserDetails principal = CustomUserDetails.fromClaims(accountId, username, roles);

                // Đưa thông tin người dùng vào Security Context (Đã đăng nhập thành công)
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (JwtException | IllegalArgumentException e) {
            // Token không hợp lệ (hết hạn, sai chữ ký, malformed...)
            log.warn("Lỗi xác thực Token: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
