package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.example.m6_thermal_power_plant_api.entity.base.CascadeSoftDelete;
import com.example.m6_thermal_power_plant_api.entity.enums.LubricationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lubrication_plans")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false, of = "id")
public class LubricationPlan extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Mã kế hoạch bảo dưỡng
     * VD: LP-20260722-001
     */
    @Column(name = "lubrication_code", length = 50, unique = true)
    private String lubricationCode;

    /**
     * Thiết bị cần bảo dưỡng
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    @CascadeSoftDelete
    private Equipment equipment;

    /**
     * Chu kỳ bảo dưỡng
     * 7, 30, 90, 180 (ngày)
     */
    @Column(name = "cycle_days", nullable = false)
    private Integer cycleDays;

    /**
     * Ngày bảo dưỡng tiếp theo
     */
    @Column(name = "next_due_date", nullable = false)
    private LocalDate nextDueDate;

    /**
     * Dầu/mỡ sử dụng
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumable_id", nullable = false)
    @CascadeSoftDelete
    private Consumable consumable;

    /**
     * Số lượng dầu/mỡ cần dùng
     */
    @Column(name = "quantity", precision = 10, scale = 2, nullable = false)
    private BigDecimal quantity;

    /**
     * Trạng thái bảo dưỡng
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private LubricationStatus status =
            LubricationStatus.NOT_LUBRICATED;
}