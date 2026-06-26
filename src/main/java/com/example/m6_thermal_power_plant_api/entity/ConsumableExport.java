package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lịch sử xuất kho vật tư tiêu hao.
 * Table: consumable_exports
 */
@Entity
@Table(name = "consumable_exports")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false, of = "id")
public class ConsumableExport extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "export_code", nullable = false, length = 50)
    private String exportCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumable_issue_id", nullable = false)
    private ConsumableIssue consumableIssue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumable_id", nullable = false)
    private Consumable consumable;

    @Column(name = "requested_quantity", precision = 10, scale = 2, nullable = false)
    private BigDecimal requestedQuantity;

    @Column(name = "actual_quantity", precision = 10, scale = 2, nullable = false)
    private BigDecimal actualQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exported_by")
    private Account exportedBy;

    @Column(name = "exported_at")
    private LocalDateTime exportedAt;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(length = 50)
    private String status;
}
