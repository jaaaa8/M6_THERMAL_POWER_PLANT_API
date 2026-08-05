package com.example.m6_thermal_power_plant_api.service;

import com.example.m6_thermal_power_plant_api.dto.ChangePasswordRequestDTO;
import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.exception.BadRequestException;
import com.example.m6_thermal_power_plant_api.exception.ResourceNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.AccountRepository;
import com.example.m6_thermal_power_plant_api.repository.RefreshTokenRepository;
import com.example.m6_thermal_power_plant_api.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void changePassword_success() {
        // Arrange
        Integer accountId = 1;
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setOldPassword("old_password");
        request.setNewPassword("new_password");

        Account account = Account.builder()
                .id(accountId)
                .username("testuser")
                .passwordHash("encoded_old_password")
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("old_password", "encoded_old_password")).thenReturn(true);
        when(passwordEncoder.encode("new_password")).thenReturn("encoded_new_password");
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // Act
        authService.changePassword(accountId, request);

        // Assert
        assertThat(account.getPasswordHash()).isEqualTo("encoded_new_password");

        verify(accountRepository).findById(accountId);
        verify(passwordEncoder).matches("old_password", "encoded_old_password");
        verify(passwordEncoder).encode("new_password");
        verify(accountRepository).save(account);
        verify(refreshTokenRepository).deleteByAccount(account);
        verify(refreshTokenRepository).flush();
    }

    @Test
    void changePassword_whenAccountNotFound_throwsResourceNotFoundException() {
        // Arrange
        Integer accountId = 999;
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setOldPassword("old_password");
        request.setNewPassword("new_password");

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.changePassword(accountId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tài khoản không tồn tại");

        verify(accountRepository).findById(accountId);
        verifyNoInteractions(passwordEncoder, refreshTokenRepository);
    }

    @Test
    void changePassword_whenOldPasswordIncorrect_throwsBadRequestException() {
        // Arrange
        Integer accountId = 1;
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setOldPassword("wrong_old_password");
        request.setNewPassword("new_password");

        Account account = Account.builder()
                .id(accountId)
                .username("testuser")
                .passwordHash("encoded_old_password")
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("wrong_old_password", "encoded_old_password")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.changePassword(accountId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Mật khẩu cũ không chính xác");

        verify(accountRepository).findById(accountId);
        verify(passwordEncoder).matches("wrong_old_password", "encoded_old_password");
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void changePassword_whenNewPasswordSameAsOldPassword_throwsBadRequestException() {
        // Arrange
        Integer accountId = 1;
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setOldPassword("same_password");
        request.setNewPassword("same_password");

        Account account = Account.builder()
                .id(accountId)
                .username("testuser")
                .passwordHash("encoded_old_password")
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("same_password", "encoded_old_password")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.changePassword(accountId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Mật khẩu mới không được trùng với mật khẩu cũ");

        verify(accountRepository).findById(accountId);
        verify(passwordEncoder).matches("same_password", "encoded_old_password");
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoInteractions(refreshTokenRepository);
    }
}
