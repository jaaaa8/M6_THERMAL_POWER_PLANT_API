package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lịch sử nhập kho vật tư tiêu hao.
 * Table: consumable_receipts
 */
@Entity
@Table(name = "consumable_receipts")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false, of = "id")
public class ConsumableReceipt extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "receipt_code", nullable = false, length = 50)
    private String receiptCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumable_id")
    private Consumable consumable;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal quantity;

    @Column(length = 255)
    private String supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_by")
    private Account receivedBy;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;
}
