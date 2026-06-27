package com.example.m6_thermal_power_plant_api.service;

import com.example.m6_thermal_power_plant_api.dto.LoginRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.LoginResponseDTO;
import com.example.m6_thermal_power_plant_api.dto.RefreshTokenRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.RefreshTokenResponseDTO;
import com.example.m6_thermal_power_plant_api.dto.UserInfoDTO;
import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.Employee;
import com.example.m6_thermal_power_plant_api.entity.RefreshToken;
import com.example.m6_thermal_power_plant_api.exception.InvalidCredentialsException;
import com.example.m6_thermal_power_plant_api.exception.InvalidTokenException;
import com.example.m6_thermal_power_plant_api.exception.ResourceNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.AccountRepository;
import com.example.m6_thermal_power_plant_api.repository.RefreshTokenRepository;
import com.example.m6_thermal_power_plant_api.security.CustomUserDetails;
import com.example.m6_thermal_power_plant_api.security.JwtUtils;
import com.example.m6_thermal_power_plant_api.security.TokenHasher;
import com.example.m6_thermal_power_plant_api.service.impl.IAuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService implements IAuthService {
    private static final String ROLE_PREFIX = "ROLE_";

    private final AccountRepository accountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @Value("${scms.jwt.refresh-token-expiration}")
    private long refreshExpiration;

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        // Để Spring Security tự verify (DaoAuthenticationProvider + BCrypt).
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException | UsernameNotFoundException ex) {
            // Gộp 2 case → không lộ "user tồn tại / không tồn tại"
            throw new InvalidCredentialsException("Tên đăng nhập hoặc mật khẩu không đúng");
        } catch (DisabledException ex) {
            throw new InvalidCredentialsException("Tài khoản đã bị khoá");
        }

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        // Chỉ lấy authority có prefix ROLE_ (loại trừ các authority khác như permission)
        List<String> roleList = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith(ROLE_PREFIX))
                .map(a -> a.substring(ROLE_PREFIX.length()))
                .toList();

        String accessToken = jwtUtils.generateAccessToken(principal.getAccountId(), principal.getUsername(), roleList);
        String refreshToken = jwtUtils.generateRefreshToken(principal.getUsername());

        // Fetch đầy đủ employee + department để build UserInfoDTO (eager qua @EntityGraph)
        Account account = accountRepository.findWithEmployeeById(principal.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại"));

        // Xoá refresh token cũ (1 user 1 session theo @OneToOne hiện tại)
        refreshTokenRepository.deleteByAccount(account);
        refreshTokenRepository.flush();

        saveRefreshToken(refreshToken, account);

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(toUserInfo(account, roleList))
                .build();
    }

    @Override
    public UserInfoDTO getMe(Integer accountId) {
        Account account = accountRepository.findWithEmployeeById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại"));
        List<String> roles = account.getRoles().stream().map(r -> r.getName()).toList();
        return toUserInfo(account, roles);
    }

    private UserInfoDTO toUserInfo(Account account, List<String> roles) {
        Employee emp = account.getEmployee();
        String fullName = (emp != null && emp.getFullName() != null) ? emp.getFullName() : account.getUsername();
        return UserInfoDTO.builder()
                .accountId(account.getId())
                .username(account.getUsername())
                .fullName(fullName)
                .roles(roles)
                .employeeCode(emp != null ? emp.getEmployeeCode() : null)
                .departmentName(emp != null && emp.getDepartment() != null ? emp.getDepartment().getName() : null)
                .position(emp != null ? emp.getPosition() : null)
                .avatarUrl(emp != null ? emp.getImgPath() : null)
                .build();
    }

    @Override
    public RefreshTokenResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        String requestRefreshToken = request.getRefreshToken();
        String hash = TokenHasher.sha256(requestRefreshToken);

        RefreshToken dbToken = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidTokenException("Refresh Token không hợp lệ hoặc đã bị thu hồi. Vui lòng đăng nhập lại!"));

        // Parse JWT 1 lần — bắt mọi lỗi JWT (hết hạn, sai chữ ký, malformed) → 401
        Claims claims;
        try {
            claims = jwtUtils.parse(requestRefreshToken);
        } catch (JwtException | IllegalArgumentException ex) {
            // Token JWT vô hiệu → xoá luôn row DB để dọn rác
            refreshTokenRepository.delete(dbToken);
            throw new InvalidTokenException("Refresh Token không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại!");
        }

        String username = claims.getSubject();
        if (username == null) {
            refreshTokenRepository.delete(dbToken);
            throw new InvalidTokenException("Refresh Token không hợp lệ");
        }

        Account account = accountRepository.findAccountByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại"));

        if (!dbToken.getAccount().getId().equals(account.getId())) {
            throw new InvalidTokenException("Cảnh báo bảo mật: Token không khớp với chủ tài khoản!");
        }

        // expiresAt trong DB là nguồn sự thật thứ hai (ngoài exp của JWT)
        if (dbToken.getExpiresAt().before(new Date())) {
            refreshTokenRepository.delete(dbToken);
            throw new InvalidTokenException("Refresh Token đã hết hạn. Vui lòng đăng nhập lại!");
        }

        List<String> roles = account.getRoles().stream().map(r -> r.getName()).toList();
        String newAccessToken = jwtUtils.generateAccessToken(account.getId(), username, roles);
        String newRefreshToken = jwtUtils.generateRefreshToken(username);

        // ROTATION: xoá token cũ, lưu hash của token mới.
        // @Version trên RefreshToken sẽ ném OptimisticLockingFailureException
        // nếu 2 request rotate cùng row đồng thời → handler trả 409.
        refreshTokenRepository.delete(dbToken);
        refreshTokenRepository.flush();
        saveRefreshToken(newRefreshToken, account);

        return RefreshTokenResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Override
    public void logout(String username) {
        Account account = accountRepository.findAccountByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));
        refreshTokenRepository.deleteByAccount(account);
        refreshTokenRepository.flush();
    }

    private void saveRefreshToken(String plainRefreshToken, Account account) {
        RefreshToken entity = RefreshToken.builder()
                .tokenHash(TokenHasher.sha256(plainRefreshToken))
                .account(account)
                .expiresAt(new Date(System.currentTimeMillis() + refreshExpiration))
                .build();
        refreshTokenRepository.saveAndFlush(entity);
    }
}
