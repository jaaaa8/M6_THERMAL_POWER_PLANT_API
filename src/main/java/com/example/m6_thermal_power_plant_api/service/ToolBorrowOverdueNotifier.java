package com.example.m6_thermal_power_plant_api.service;

import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.enums.BorrowStatus;
import com.example.m6_thermal_power_plant_api.entity.tool.ToolBorrowLog;
import com.example.m6_thermal_power_plant_api.repository.IToolBorrowLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    /** Chạy mỗi giờ để kiểm tra các phiếu mượn quá hạn chưa được nhắc nhở. */
    @Scheduled(cron = "0 0 * * * *")
    public void notifyOverdueBorrows() {
        sendOverdueNotifications();
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

    private SimpleMailMessage buildOverdueMessage(String toEmail, ToolBorrowLog borrowLog) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[Nhắc nhở] Quá hạn trả công cụ dụng cụ: " + borrowLog.getTool().getName());
        message.setText("""
                Xin chào,

                Bạn đang mượn CCDC sau và đã quá hạn trả:
                - Mã CCDC: %s
                - Tên CCDC: %s
                - Số lượng: %d
                - Hạn trả: %s

                Vui lòng liên hệ thủ kho để hoàn trả sớm nhất có thể.
                """.formatted(
                borrowLog.getTool().getToolCode(),
                borrowLog.getTool().getName(),
                borrowLog.getQuantity(),
                borrowLog.getDueDate().format(DATE_FORMAT)
        ));
        return message;
    }
}
