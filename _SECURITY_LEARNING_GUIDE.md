# 🔐 Security Rewrite Learning Guide

> **Branch:** `learn/security-rewrite`
> **Bản gốc backup:** `_security_original/` (chỉ mở khi bí!)
> **Mục tiêu:** Tự viết lại toàn bộ phần security để hiểu sâu và ghi nhớ

---

## 📋 Danh sách file cần viết lại (theo thứ tự)

### Tầng 1 — Cấu hình (Foundation)
| # | File | Package | Mô tả |
|---|------|---------|-------|
| 1 | `SecurityConfig.java` | `security` | FilterChain, PasswordEncoder, AuthenticationManager |
| 2 | `CorsConfig.java` | `security` | CORS configuration cho frontend |

### Tầng 2 — Identity (Spring Security adapter)
| # | File | Package | Mô tả |
|---|------|---------|-------|
| 3 | `CustomUserDetails.java` | `security` | Implement UserDetails, 2 factory method |
| 4 | `CustomUserDetailsService.java` | `security` | Load Account từ DB theo username |

### Tầng 3 — JWT Core
| # | File | Package | Mô tả |
|---|------|---------|-------|
| 5 | `JwtUtils.java` | `security` | Sinh access/refresh token, parse, verify |
| 6 | `TokenHasher.java` | `security` | SHA-256 hash refresh token |
| 7 | `JwtAuthenticationFilter.java` | `security` | Filter đọc Bearer token, set SecurityContext |
| 8 | `JwtAuthenticationEntryPoint.java` | `security` | Trả JSON 401 khi chưa đăng nhập |

### Tầng 4 — Business Logic
| # | File | Package | Mô tả |
|---|------|---------|-------|
| 9 | `IAuthService.java` | `service.impl` | Interface định nghĩa login/refresh/logout/getMe |
| 10 | `AuthService.java` | `service` | Implement logic auth + refresh token rotation |
| 11 | `AuthController.java` | `controller.auth` | REST endpoints: /login, /refresh, /logout, /me |

---

## 🗺️ Thứ tự xây dựng đề xuất

```
Bước 1: SecurityConfig + CorsConfig
         ↓ (app compile được nhưng chưa có JWT)
Bước 2: CustomUserDetails + CustomUserDetailsService
         ↓ (Spring có thể load user từ DB)
Bước 3: JwtUtils + TokenHasher
         ↓ (có thể sinh/verify token)
Bước 4: JwtAuthenticationFilter + JwtAuthenticationEntryPoint
         ↓ (SecurityConfig có thể addFilterBefore)
Bước 5: Quay lại sửa SecurityConfig để wire filter + entry point
         ↓ (Security hoàn chỉnh!)
Bước 6: IAuthService + AuthService + AuthController
         ↓ (API login/refresh/logout/me hoạt động)
Bước 7: Test bằng Postman/curl
```

---

## 📚 Kiến thức nền cần có

- **Spring Security 6.x**: SecurityFilterChain, AuthenticationManager, DaoAuthenticationProvider
- **JWT (JSON Web Token)**: Header.Payload.Signature, HS256, claims, expiration
- **jjwt library**: `io.jsonwebtoken.*` — Jwts.builder(), parserBuilder(), Claims
- **Spring Security Filter Chain**: OncePerRequestFilter, SecurityContextHolder
- **BCrypt**: Password hashing, salt tự động
- **Refresh Token Rotation**: Pattern bảo mật, optimistic locking (@Version)

---

## 📖 Thông tin config (đã có sẵn trong application.properties)

```properties
scms.jwt.base64-secret=<your-secret>
scms.jwt.access-token-expiration=<ms>
scms.jwt.refresh-token-expiration=<ms>
```

---

## ✅ Checklist sau mỗi bước

- [ ] Code compile không lỗi
- [ ] Hiểu được MỖI dòng code làm gì
- [ ] Có thể giải thích tại sao dùng cái này thay vì cái khác
- [ ] Test được (ít nhất compile + context load)

---

## 💡 Quy tắc học

1. **Viết code TRƯỚC**, mở `_security_original/` SAU (chỉ khi bí > 15 phút)
2. **Comment tiếng Việt** — giải thích tại sao, không chỉ làm gì
3. **Chạy compile** sau mỗi file hoàn thành
4. **Đặt câu hỏi** cho mình bất cứ lúc nào — không có câu hỏi ngớ ngẩn
5. **So sánh** code của bạn với bản gốc sau khi xong — tìm điểm khác và hiểu tại sao
