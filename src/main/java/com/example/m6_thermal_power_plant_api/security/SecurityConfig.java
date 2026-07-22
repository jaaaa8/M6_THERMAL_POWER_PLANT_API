package com.example.m6_thermal_power_plant_api.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint jwtAuthEntryPoint;

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        var provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {}) // Bật CORS, cấu hình trong CorsConfig
                .csrf(csrf -> csrf.disable()) // Tắt CSRF vì chúng ta dùng JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Không lưu session
                .authorizeHttpRequests(auth -> auth
                        // Mở cửa tự do cho đường dẫn đăng nhập
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/auth/refresh").permitAll()
                        .requestMatchers("/api/v1/accounts/create").permitAll()
                        .requestMatchers("/api/images/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/api/v1/tool-borrow-logs/test-email").permitAll()
                        // Tất cả các request khác đều phải có token hợp lệ
                        .anyRequest().authenticated()
                )
                // Trả JSON 401 khi chưa đăng nhập, thay vì 403 HTML mặc định
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthEntryPoint))
                // Nhét cái phễu lọc JwtFilter của chúng ta vào trước bộ lọc mặc định của Spring
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Cây phân cấp vai trò: ADMIN đứng trên MỌI role khác → hasAnyRole('X') tự pass
     * cho ADMIN mà không phải liệt kê ADMIN ở từng @PreAuthorize. Đây là cách xử lý
     * dứt điểm "ADMIN full quyền" (thay cho special-case cũ từng gây blocker 403).
     */
    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("ADMIN").implies(
                        "WORKER", "MATERIALS_STOREKEEPER", "TOOLS_STOREKEEPER", "WORKSHOP_FOREMAN",
                        "SHIFT_LEADER", "CREW_LEADER", "MAINTENANCE_FOREMAN", "TEAM_LEADER",
                        "SAFETY_SUPERVISOR", "HR_STAFF")
                .build();
    }

    /**
     * Cho @PreAuthorize/@PostAuthorize áp RoleHierarchy ở trên. Bean PHẢI static để
     * khởi tạo sớm trước method-security interceptor (khuyến nghị của Spring Security).
     */
    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        return handler;
    }
}