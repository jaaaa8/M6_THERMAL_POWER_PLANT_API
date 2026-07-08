package com.example.m6_thermal_power_plant_api.security;

import com.example.m6_thermal_power_plant_api.repository.AccountRepository;
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
    private final AccountRepository accountRepository;

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
                List<String> permissions = claims.get("permissions", List.class);
                if (permissions == null) permissions = Collections.emptyList();
                Number accountIdNum = claims.get("accountId", Number.class);
                Integer accountId = accountIdNum != null ? accountIdNum.intValue() : null;
                Number permVerNum = claims.get("permVer", Number.class);
                Integer tokenPermVer = permVerNum != null ? permVerNum.intValue() : null;

                // CÁCH 2: so 1 con số permVer với DB, KHÔNG load lại toàn bộ permission.
                // Lệch → coi token này "cũ", không set Context → downstream trả 401,
                // buộc client gọi /auth/refresh để lấy permission mới nhất.
                Integer currentPermVer = accountId != null
                        ? accountRepository.findPermissionVersionById(accountId).orElse(null)
                        : null;

                if (accountId != null && currentPermVer != null && currentPermVer.equals(tokenPermVer)) {
                    CustomUserDetails principal = CustomUserDetails.fromClaims(accountId, username, roles, permissions);

                    // Đưa thông tin người dùng vào Security Context (Đã đăng nhập thành công)
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            principal, null, principal.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    log.warn("Permission version lệch (token={}, hiện tại={}) cho accountId={} — yêu cầu refresh",
                            tokenPermVer, currentPermVer, accountId);
                }
            }
        } catch (JwtException | IllegalArgumentException e) {
            // Token không hợp lệ (hết hạn, sai chữ ký, malformed...)
            log.warn("Lỗi xác thực Token: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}