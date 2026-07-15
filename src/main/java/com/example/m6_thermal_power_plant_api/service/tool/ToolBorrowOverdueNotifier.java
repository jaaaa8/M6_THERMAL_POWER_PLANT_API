package com.example.m6_thermal_power_plant_api.service.tool;

import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.enums.BorrowStatus;
import com.example.m6_thermal_power_plant_api.entity.tool.ToolBorrowLog;
import com.example.m6_thermal_power_plant_api.repository.IToolBorrowLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Quét định kỳ các phiếu mượn CCDC đã quá hạn (APPROVED, chưa trả, đã qua dueDate)
 * và gửi email nhắc nhở tới nhân viên mượn — mỗi phiếu chỉ gửi 1 lần (overdueNotified).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ToolBorrowOverdueNotifier {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final IToolBorrowLogRepository toolBorrowLogRepository;
    private final JavaMailSender mailSender;

    @Scheduled(cron = "0 */2 * * * *")
    @Transactional
    public void notifyOverdueBorrows() {
        sendOverdueNotifications();
    }

    @Scheduled(cron = "0 */2 * * * *")
    @Transactional
    public void notifyDueSoonBorrows() {
        sendDueSoonNotifications();
    }

    /**
     * Quét và gửi email nhắc quá hạn ngay lập tức (dùng cho job định kỳ lẫn
     * trigger thủ công từ thủ kho). Trả về số email gửi thành công.
     */
    @Transactional
    public int sendOverdueNotifications() {
        List<ToolBorrowLog> overdueLogs = toolBorrowLogRepository
                .findByStatusAndDueDateBeforeAndActualReturnDateIsNullAndOverdueNotifiedFalse(
                        BorrowStatus.APPROVED, LocalDateTime.now());

        int sentCount = 0;
        for (ToolBorrowLog borrowLog : overdueLogs) {
            Account account = borrowLog.getAccount();
            String email = account.getEmployee() == null ? null : account.getEmployee().getGmail();
            if (email == null || email.isBlank()) {
                continue;
            }

            try {
                mailSender.send(buildOverdueMessage(email, borrowLog));
                borrowLog.setOverdueNotified(true);
                toolBorrowLogRepository.save(borrowLog);
                sentCount++;
            } catch (Exception ex) {
                log.error("Gửi email nhắc quá hạn thất bại cho phiếu mượn id={}", borrowLog.getId(), ex);
            }
        }
        return sentCount;
    }

    @Transactional
    public int sendDueSoonNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusHours(24);

        List<ToolBorrowLog> dueSoonLogs = toolBorrowLogRepository
                .findByStatusAndDueDateBetweenAndActualReturnDateIsNullAndDueSoonNotifiedFalse(
                        BorrowStatus.APPROVED, now, tomorrow);

        int sentCount = 0;
        for (ToolBorrowLog borrowLog : dueSoonLogs) {
            Account account = borrowLog.getAccount();
            String email = account.getEmployee() == null ? null : account.getEmployee().getGmail();
            if (email == null || email.isBlank()) {
                continue;
            }

            try {
                mailSender.send(buildDueSoonMessage(email, borrowLog));
                borrowLog.setDueSoonNotified(true);
                toolBorrowLogRepository.save(borrowLog);
                sentCount++;
            } catch (Exception ex) {
                log.error("Gửi email nhắc sắp đến hạn thất bại cho phiếu mượn id={}", borrowLog.getId(), ex);
            }
        }
        return sentCount;
    }

    private MimeMessage buildDueSoonMessage(String toEmail, ToolBorrowLog borrowLog) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("[Nhắc nhở] Sắp đến hạn trả CCDC ngày mai: " + borrowLog.getTool().getName());
            helper.setText("""
                    Xin chào,

                    Bạn đang mượn CCDC sau và sẽ đến hạn trả vào ngày mai:
                    - Mã CCDC: %s
                    - Tên CCDC: %s
                    - Số lượng: %d
                    - Hạn trả: %s

                    Vui lòng chuẩn bị hoàn trả đúng hạn để tránh bị ghi nhận vi phạm.

                    Trân trọng,
                    Phòng Quản lý CCDC - Nhà máy Nhiệt điện
                    """.formatted(
                    borrowLog.getTool().getToolCode(),
                    borrowLog.getTool().getName(),
                    borrowLog.getQuantity(),
                    borrowLog.getDueDate().format(DATE_FORMAT)
            ));
            return message;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo email sắp hết hạn", e);
        }
    }

    public void sendTestEmail(String toEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("[Test] Kiểm tra cấu hình email SCMS");
            helper.setText("""
                    Xin chào,

                    Đây là email test từ hệ thống SCMS - Nhà máy Nhiệt điện.
                    Nếu bạn nhận được email này, cấu hình SMTP đang hoạt động đúng.

                    Trân trọng,
                    Hệ thống SCMS
                    """);
            mailSender.send(message);
            log.info("Đã gửi email test đến {}", toEmail);
        } catch (Exception e) {
            log.error("Gửi email test thất bại", e);
        }
    }

    private MimeMessage buildOverdueMessage(String toEmail, ToolBorrowLog borrowLog) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("[Nhắc nhở] Quá hạn trả CCDC: " + borrowLog.getTool().getName());
            helper.setText("""
                    Xin chào,

                    Bạn đang mượn CCDC sau và đã quá hạn trả:
                    - Mã CCDC: %s
                    - Tên CCDC: %s
                    - Số lượng: %d
                    - Hạn trả: %s

                    Vui lòng liên hệ thủ kho để hoàn trả sớm nhất có thể.

                    Trân trọng,
                    Phòng Quản lý CCDC - Nhà máy Nhiệt điện
                    """.formatted(
                    borrowLog.getTool().getToolCode(),
                    borrowLog.getTool().getName(),
                    borrowLog.getQuantity(),
                    borrowLog.getDueDate().format(DATE_FORMAT)
            ));
            return message;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo email quá hạn", e);
        }
    }
}
