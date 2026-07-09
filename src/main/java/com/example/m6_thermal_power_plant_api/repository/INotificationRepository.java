package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface INotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByRecipientAccountIdOrderByCreatedAtDesc(Integer accountId);

    long countByRecipientAccountIdAndIsReadFalse(Integer accountId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id AND n.recipientAccountId = :accountId")
    void markAsRead(@Param("id") Integer id, @Param("accountId") Integer accountId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipientAccountId = :accountId")
    void markAllAsRead(@Param("accountId") Integer accountId);
}
