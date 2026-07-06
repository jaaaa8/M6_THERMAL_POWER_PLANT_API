package com.example.m6_thermal_power_plant_api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Component
public class JwtUtils {

    @Value("${scms.jwt.base64-secret}")
    private String jwtSecret;

    @Value("${scms.jwt.access-token-expiration}")
    private long jwtExpiration;

    @Value("${scms.jwt.refresh-token-expiration}")
    private long refreshExpiration;

    // Hàm 1: Sinh ra token chứa accountId, username, roles, permissions và permVer
    // (permVer = Account.permissionVersion tại thời điểm phát hành — dùng để
    // jwtAuthFilter phát hiện permission đã cũ, xem Account.permissionVersion)
    public String generateAccessToken(Integer accountId, String username, List<String> roles,
                                       List<String> permissions, Integer permVer) {
        return Jwts.builder()
                .setSubject(username)
                .claim("accountId", accountId)
                .claim("roles", roles)
                .claim("permissions", permissions)
                .claim("permVer", permVer)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Parse + verify token một lần, trả về Claims (dùng cho filter để tránh parse nhiều lần)
    public Claims parse(String token) {
        return extractAllClaims(token);
    }

    // Hàm 2: Lấy Username từ Token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Hàm 3: Lấy danh sách Roles từ Token
    public List<String> extractRoles(String token) {
        return extractClaim(token, (c) -> c.get("roles", List.class));
    }

    // Hàm 4: Kiểm tra token còn hợp lệ không
    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (username.equals(extractedUsername)) && !isTokenExpired(token);
    }

    // Hàm 5: Sinh ra Refresh Token (Chỉ chứa username, sống lâu hơn)
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
