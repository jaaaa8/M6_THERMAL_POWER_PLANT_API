package com.example.m6_thermal_power_plant_api.controller.auth;

import com.example.m6_thermal_power_plant_api.dto.LoginRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.LoginResponseDTO;
import com.example.m6_thermal_power_plant_api.dto.RefreshTokenRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.RefreshTokenResponseDTO;
import com.example.m6_thermal_power_plant_api.dto.UserInfoDTO;
import com.example.m6_thermal_power_plant_api.dto.ChangePasswordRequestDTO;
import com.example.m6_thermal_power_plant_api.exception.ApiResponse;
import com.example.m6_thermal_power_plant_api.security.CustomUserDetails;
import com.example.m6_thermal_power_plant_api.service.impl.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final IAuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authService.login(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponseDTO>> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        RefreshTokenResponseDTO response = authService.refreshToken(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(Principal principal) {
        authService.logout(principal.getName());
        return ApiResponse.success("Đăng xuất thành công!");
    }

    /**
     * Trả về thông tin user hiện tại từ access token.
     * FE gọi sau khi reload trang (F5) để khôi phục state mà không cần login lại.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoDTO>> getMe(@AuthenticationPrincipal CustomUserDetails me) {
        UserInfoDTO info = authService.getMe(me.getAccountId());
        return ApiResponse.success(info);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @AuthenticationPrincipal CustomUserDetails me,
            @Valid @RequestBody ChangePasswordRequestDTO request) {
        authService.changePassword(me.getAccountId(), request);
        return ApiResponse.success("Đổi mật khẩu thành công!");
    }
}
