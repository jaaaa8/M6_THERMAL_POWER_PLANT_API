package com.example.m6_thermal_power_plant_api.service.auth;

import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.Employee;
import com.example.m6_thermal_power_plant_api.entity.PasswordOtp;
import com.example.m6_thermal_power_plant_api.exception.BadRequestException;
import com.example.m6_thermal_power_plant_api.repository.AccountRepository;
import com.example.m6_thermal_power_plant_api.repository.PasswordOtpRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Sáu nhánh của vòng đời mã OTP. Đây là test đầu tiên của khu vực auth —
 * trước đó AuthService không có test nào.
 */
@ExtendWith(MockitoExtension.class)
class PasswordOtpServiceTest {

    private static final String RIGHT_OTP = "123456";
    private static final String OTP_HASH = "$2a$10$hashOfTheOtp";

    @Mock
    private PasswordOtpRepository passwordOtpRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JavaMailSender mailSender;
    @InjectMocks
    private PasswordOtpService passwordOtpService;

    /* ── verifyAndConsume ─────────────────────────────────────────────── */

    @Test
    void verifyAndConsume_marksOtpUsed_whenCodeCorrectAndFresh() {
        PasswordOtp otp = otp(LocalDateTime.now().plusMinutes(3), 0);
        when(passwordOtpRepository
                .findFirstByAccount_IdAndConsumedAtIsNullOrderByCreatedAtDesc(1))
                .thenReturn(Optional.of(otp));
        when(passwordEncoder.matches(RIGHT_OTP, OTP_HASH)).thenReturn(true);

        passwordOtpService.verifyAndConsume(1, RIGHT_OTP);

        assertThat(otp.getConsumedAt()).isNotNull();
        verify(passwordOtpRepository).save(otp);
    }

    @Test
    void verifyAndConsume_incrementsAndPersistsAttempts_whenCodeWrong() {
        PasswordOtp otp = otp(LocalDateTime.now().plusMinutes(3), 2);
        when(passwordOtpRepository
                .findFirstByAccount_IdAndConsumedAtIsNullOrderByCreatedAtDesc(1))
                .thenReturn(Optional.of(otp));
        when(passwordEncoder.matches("000000", OTP_HASH)).thenReturn(false);

        assertThatThrownBy(() -> passwordOtpService.verifyAndConsume(1, "000000"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("không đúng");

        // Bộ đếm phải được GHI XUỐNG DB, không chỉ tăng trong bộ nhớ rồi ném
        // exception — không lưu thì trần số lần thử là vô nghĩa.
        assertThat(otp.getAttempts()).isEqualTo(3);
        assertThat(otp.getConsumedAt()).isNull();
        verify(passwordOtpRepository).save(otp);
    }

    @Test
    void verifyAndConsume_rejects_whenExpired() {
        PasswordOtp otp = otp(LocalDateTime.now().minusSeconds(1), 0);
        when(passwordOtpRepository
                .findFirstByAccount_IdAndConsumedAtIsNullOrderByCreatedAtDesc(1))
                .thenReturn(Optional.of(otp));

        assertThatThrownBy(() -> passwordOtpService.verifyAndConsume(1, RIGHT_OTP))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("hết hạn");

        assertThat(otp.getConsumedAt()).isNull();
    }

    @Test
    void verifyAndConsume_rejects_whenNoOtpRequestedYet() {
        // Mã đã dùng rồi cũng rơi vào đây: repository chỉ trả mã CHƯA dùng.
        when(passwordOtpRepository
                .findFirstByAccount_IdAndConsumedAtIsNullOrderByCreatedAtDesc(1))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordOtpService.verifyAndConsume(1, RIGHT_OTP))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Gửi mã");
    }

    @Test
    void verifyAndConsume_rejectsEvenCorrectCode_whenAttemptsExhausted() {
        // Case dễ sót nhất: trần số lần sai phải được xét TRƯỚC khi so mã, nếu
        // không kẻ dò đủ 5 lần vẫn còn cơ hội thứ 6.
        PasswordOtp otp = otp(LocalDateTime.now().plusMinutes(3), 5);
        when(passwordOtpRepository
                .findFirstByAccount_IdAndConsumedAtIsNullOrderByCreatedAtDesc(1))
                .thenReturn(Optional.of(otp));

        assertThatThrownBy(() -> passwordOtpService.verifyAndConsume(1, RIGHT_OTP))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("quá 5 lần");

        assertThat(otp.getConsumedAt()).isNull();
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    /* ── requestOtp ───────────────────────────────────────────────────── */

    @Test
    void requestOtp_rejects_whenPreviousOtpStillWithinCooldown() {
        Account account = account("nguyenhieu@example.com", null);
        when(accountRepository.findById(1)).thenReturn(Optional.of(account));
        PasswordOtp recent = otp(LocalDateTime.now().plusMinutes(4), 0);
        recent.setCreatedAt(LocalDateTime.now().minusSeconds(5));
        when(passwordOtpRepository
                .findFirstByAccount_IdAndConsumedAtIsNullOrderByCreatedAtDesc(1))
                .thenReturn(Optional.of(recent));

        assertThatThrownBy(() -> passwordOtpService.requestOtp(1))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("đợi");

        verify(mailSender, never()).send(any(MimeMessage.class));
        verify(passwordOtpRepository, never()).save(any());
    }

    @Test
    void requestOtp_sendsMailAndReturnsMaskedEmail() {
        Account account = account("nguyenhieu@example.com", null);
        when(accountRepository.findById(1)).thenReturn(Optional.of(account));
        when(passwordOtpRepository
                .findFirstByAccount_IdAndConsumedAtIsNullOrderByCreatedAtDesc(1))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn(OTP_HASH);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage());

        String masked = passwordOtpService.requestOtp(1);

        assertThat(masked).isEqualTo("ng***@example.com");
        verify(passwordOtpRepository).save(any(PasswordOtp.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void requestOtp_fallsBackToEmployeeGmail_whenAccountEmailMissing() {
        Account account = account(null, "tho.sua@example.com");
        when(accountRepository.findById(1)).thenReturn(Optional.of(account));
        when(passwordOtpRepository
                .findFirstByAccount_IdAndConsumedAtIsNullOrderByCreatedAtDesc(1))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn(OTP_HASH);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage());

        assertThat(passwordOtpService.requestOtp(1)).isEqualTo("th***@example.com");
    }

    @Test
    void requestOtp_failsLoudly_whenAccountHasNoEmailAtAll() {
        // KHÔNG được im lặng báo "đã gửi" — người dùng sẽ ngồi đợi mã không tới.
        Account account = account(null, null);
        when(accountRepository.findById(1)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> passwordOtpService.requestOtp(1))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("chưa có địa chỉ email");

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    /* ── helpers ──────────────────────────────────────────────────────── */

    private static PasswordOtp otp(LocalDateTime expiresAt, int attempts) {
        PasswordOtp otp = PasswordOtp.builder()
                .id(1)
                .otpHash(OTP_HASH)
                .expiresAt(expiresAt)
                .attempts(attempts)
                .build();
        otp.setCreatedAt(LocalDateTime.now().minusMinutes(2));
        return otp;
    }

    private static Account account(String accountEmail, String employeeGmail) {
        Employee employee = employeeGmail == null ? null
                : Employee.builder().id(1).employeeCode("EMP-1").gmail(employeeGmail).build();
        return Account.builder()
                .id(1).username("u").passwordHash("x")
                .email(accountEmail).employee(employee)
                .build();
    }

    /** MimeMessage thật (không mock) — helper chỉ set To/Subject/Text lên nó. */
    private static MimeMessage mimeMessage() {
        return new jakarta.mail.internet.MimeMessage((jakarta.mail.Session) null);
    }
}
