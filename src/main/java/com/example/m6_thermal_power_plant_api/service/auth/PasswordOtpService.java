package com.example.m6_thermal_power_plant_api.service.auth;

import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.PasswordOtp;
import com.example.m6_thermal_power_plant_api.exception.BadRequestException;
import com.example.m6_thermal_power_plant_api.exception.ResourceNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.AccountRepository;
import com.example.m6_thermal_power_plant_api.repository.PasswordOtpRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Sinh, gửi và xác thực mã OTP xác nhận đổi mật khẩu.
 *
 * Tách riêng khỏi {@code AuthService} (vốn đã dài) vì đây là một mối bận tâm
 * độc lập: vòng đời của mã, gửi mail, và chống lạm dụng.
 *
 * OTP là lớp xác nhận THỨ HAI — không thay thế mật khẩu cũ. Luồng đổi mật khẩu
 * vẫn kiểm mật khẩu cũ như trước.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordOtpService {

    /** 6 chữ số: quen thuộc, gõ nhanh trên điện thoại. */
    private static final int OTP_LENGTH = 6;
    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    /** Không có trần này thì mã 6 chữ số bị dò cạn trong ~1 triệu request. */
    private static final int MAX_ATTEMPTS = 5;
    /** Giãn cách giữa 2 lần xin mã — chặn biến hệ thống thành máy spam mail. */
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);

    /** SecureRandom, KHÔNG dùng Random/Math.random — đây là đường bảo mật. */
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PasswordOtpRepository passwordOtpRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    /**
     * Sinh mã mới, lưu hash, gửi vào email của tài khoản.
     *
     * @return địa chỉ email ĐÃ CHE để FE hiển thị cho người dùng biết mã đi đâu
     *         mà không lộ nguyên địa chỉ ra màn hình.
     */
    @Transactional
    public String requestOtp(Integer accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại"));

        String email = resolveEmail(account);

        // Chặn spam: mã gần nhất còn hiệu lực và vừa gửi xong thì không gửi tiếp.
        passwordOtpRepository
                .findFirstByAccount_IdAndConsumedAtIsNullOrderByCreatedAtDesc(accountId)
                .filter(otp -> otp.getCreatedAt() != null)
                .filter(otp -> otp.getCreatedAt().isAfter(LocalDateTime.now().minus(RESEND_COOLDOWN)))
                .ifPresent(otp -> {
                    throw new BadRequestException(
                            "Vui lòng đợi " + RESEND_COOLDOWN.toSeconds() + " giây trước khi yêu cầu mã mới.");
                });

        String otp = generateOtp();
        passwordOtpRepository.save(PasswordOtp.builder()
                .account(account)
                .otpHash(passwordEncoder.encode(otp))
                .expiresAt(LocalDateTime.now().plus(OTP_TTL))
                .build());

        sendOtpEmail(email, otp);
        return maskEmail(email);
    }

    /**
     * Kiểm mã người dùng nhập rồi đánh dấu ĐÃ DÙNG. Ném {@link BadRequestException}
     * với thông điệp phân biệt được cho từng lý do hỏng.
     */
    @Transactional
    public void verifyAndConsume(Integer accountId, String otp) {
        PasswordOtp record = passwordOtpRepository
                .findFirstByAccount_IdAndConsumedAtIsNullOrderByCreatedAtDesc(accountId)
                .orElseThrow(() -> new BadRequestException(
                        "Chưa có mã xác nhận nào. Vui lòng bấm \"Gửi mã\" trước."));

        if (record.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Mã xác nhận đã hết hạn. Vui lòng yêu cầu mã mới.");
        }

        // Xét trần số lần SAI TRƯỚC khi so mã: nếu so trước rồi mới xét, kẻ dò
        // đủ 5 lần vẫn còn cơ hội thứ 6 và trần trở nên vô nghĩa.
        if (record.getAttempts() >= MAX_ATTEMPTS) {
            throw new BadRequestException("Đã nhập sai quá " + MAX_ATTEMPTS
                    + " lần. Vui lòng yêu cầu mã mới.");
        }

        if (otp == null || !passwordEncoder.matches(otp, record.getOtpHash())) {
            // Lưu NGAY, không đợi cuối method: nhánh này ném exception, mà bộ đếm
            // không được ghi xuống DB thì coi như không có trần nào cả.
            record.setAttempts(record.getAttempts() + 1);
            passwordOtpRepository.save(record);
            throw new BadRequestException("Mã xác nhận không đúng.");
        }

        record.setConsumedAt(LocalDateTime.now());
        passwordOtpRepository.save(record);
    }

    /**
     * Account có email riêng, Employee có gmail — ưu tiên email của tài khoản.
     *
     * Không có địa chỉ nào thì NÉM LỖI, khác với ToolBorrowOverdueNotifier vốn
     * lặng lẽ `continue`: bỏ một mail nhắc nhở thì không sao, còn ở đây người
     * dùng sẽ ngồi đợi mã không bao giờ tới.
     */
    private String resolveEmail(Account account) {
        return Optional.ofNullable(account.getEmail())
                .filter(e -> !e.isBlank())
                .or(() -> Optional.ofNullable(account.getEmployee())
                        .map(e -> e.getGmail())
                        .filter(e -> !e.isBlank()))
                .orElseThrow(() -> new BadRequestException(
                        "Tài khoản chưa có địa chỉ email nên không gửi được mã xác nhận. "
                                + "Vui lòng liên hệ quản trị viên."));
    }

    private String generateOtp() {
        int bound = (int) Math.pow(10, OTP_LENGTH);
        return String.format("%0" + OTP_LENGTH + "d", RANDOM.nextInt(bound));
    }

    /** ng***@gmail.com — đủ để nhận ra hòm thư của mình, không lộ cả địa chỉ. */
    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 2) return "***" + email.substring(Math.max(at, 0));
        return email.substring(0, 2) + "***" + email.substring(at);
    }

    private void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("[SCMS] Mã xác nhận đổi mật khẩu");
            helper.setText("""
                    Xin chào,

                    Mã xác nhận đổi mật khẩu của bạn là: %s

                    Mã có hiệu lực trong %d phút và chỉ dùng được một lần.

                    Nếu bạn KHÔNG yêu cầu đổi mật khẩu, hãy bỏ qua email này và
                    báo ngay cho quản trị viên.

                    Trân trọng,
                    Hệ thống SCMS
                    """.formatted(otp, OTP_TTL.toMinutes()));
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Gửi email OTP đổi mật khẩu thất bại", e);
            // Ném lên để người dùng biết mà thử lại — nuốt lỗi ở đây là để họ
            // ngồi đợi một mã không bao giờ tới.
            throw new BadRequestException("Không gửi được email xác nhận. Vui lòng thử lại sau.");
        }
    }
}
