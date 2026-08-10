package com.example.m6_thermal_power_plant_api.service.tool;

import com.example.m6_thermal_power_plant_api.dto.tool.NotificationResponse;
import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.Notification;
import com.example.m6_thermal_power_plant_api.repository.INotificationRepository;
import com.example.m6_thermal_power_plant_api.repository.account.IAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private INotificationRepository notificationRepository;
    @Mock
    private IAccountRepository accountRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @InjectMocks
    private NotificationService notificationService;

    private static Account account(int id) {
        return Account.builder().id(id).build();
    }

    @Test
    void sendToRoles_sendsToEachMatchingAccountExceptExcluded() {
        when(accountRepository.findByRoleNames(List.of("SHIFT_LEADER", "CREW_LEADER", "MAINTENANCE_FOREMAN", "ADMIN")))
                .thenReturn(List.of(account(1), account(2), account(3)));

        notificationService.sendToRoles(
                List.of("SHIFT_LEADER", "CREW_LEADER", "MAINTENANCE_FOREMAN", "ADMIN"),
                "Yêu cầu sửa chữa mới", "message", "/repair/yeu-cau", 2);

        verify(notificationRepository).save(argThat(n -> n.getRecipientAccountId() == 1));
        verify(notificationRepository).save(argThat(n -> n.getRecipientAccountId() == 3));
        verify(notificationRepository, never()).save(argThat(n -> n.getRecipientAccountId() == 2));
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void sendToAdmins_preservesStorekeeperAndAdminRecipients() {
        when(accountRepository.findByRoleNames(List.of("TOOLS_STOREKEEPER", "ADMIN")))
                .thenReturn(List.of(account(1), account(2)));

        notificationService.sendToAdmins("t", "m", "l");

        verify(notificationRepository).save(argThat(n -> n.getRecipientAccountId() == 1));
        verify(notificationRepository).save(argThat(n -> n.getRecipientAccountId() == 2));
    }

    @Test
    void send_pushesWebSocketMessageForRecipient() {
        notificationService.send(7, "title", "body", "/link");

        verify(notificationRepository).save(argThat(n -> n.getRecipientAccountId() == 7));
        verify(messagingTemplate).convertAndSend(eq("/topic/notifications/7"), any(NotificationResponse.class));
    }

    @Test
    void send_withTransactionSynchronization_pushesOnlyAfterCommit() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            notificationService.send(7, "title", "body", "/link");

            verify(notificationRepository).save(argThat(n -> n.getRecipientAccountId() == 7));
            verifyNoInteractions(messagingTemplate);

            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(TransactionSynchronization::afterCommit);

            verify(messagingTemplate).convertAndSend(eq("/topic/notifications/7"), any(NotificationResponse.class));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}