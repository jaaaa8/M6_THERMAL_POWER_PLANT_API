package com.example.m6_thermal_power_plant_api.service.tool;

import com.example.m6_thermal_power_plant_api.dto.tool.NotificationResponse;
import com.example.m6_thermal_power_plant_api.entity.Notification;
import com.example.m6_thermal_power_plant_api.repository.INotificationRepository;
import com.example.m6_thermal_power_plant_api.repository.account.IAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final INotificationRepository notificationRepository;
    private final IAccountRepository accountRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /** Gửi thông báo đến 1 account cụ thể */
    public void send(Integer recipientAccountId, String title, String message, String link) {
        Notification notif = Notification.builder()
                .recipientAccountId(recipientAccountId)
                .title(title)
                .message(message)
                .link(link)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notif);
        pushToClient(notif);
    }

    /** Gửi thông báo đến tất cả tài khoản có role TOOLS_STOREKEEPER hoặc ADMIN */
    public void sendToAdmins(String title, String message, String link) {
        List<String> adminRoles = List.of("TOOLS_STOREKEEPER", "ADMIN");
        accountRepository.findByRoleNames(adminRoles).forEach(account ->
                send(account.getId(), title, message, link)
        );
    }

    public List<NotificationResponse> getByAccount(Integer accountId) {
        return notificationRepository
                .findByRecipientAccountIdOrderByCreatedAtDesc(accountId)
                .stream().map(this::toResponse).toList();
    }

    public long countUnread(Integer accountId) {
        return notificationRepository.countByRecipientAccountIdAndIsReadFalse(accountId);
    }

    public void markRead(Integer notificationId, Integer accountId) {
        notificationRepository.markAsRead(notificationId, accountId);
    }

    public void markAllRead(Integer accountId) {
        notificationRepository.markAllAsRead(accountId);
    }

    private void pushToClient(Notification notif) {
        try {
            messagingTemplate.convertAndSend(
                    "/topic/notifications/" + notif.getRecipientAccountId(),
                    toResponse(notif)
            );
        } catch (Exception e) {
            log.warn("WebSocket push failed for account {}: {}", notif.getRecipientAccountId(), e.getMessage());
        }
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .recipientAccountId(n.getRecipientAccountId())
                .title(n.getTitle())
                .message(n.getMessage())
                .link(n.getLink())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
