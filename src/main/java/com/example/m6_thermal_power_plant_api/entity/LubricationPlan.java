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

/**
 * Kế hoạch bảo dưỡng dầu mỡ cho thiết bị.
 * Table: lubrication_plans
 *
 * Soft delete: xem {@link BaseSoftDeleteEntity}. Equipment / Consumable đều đã
 * @SQLRestriction nên 2 quan hệ dưới đây KHÔNG cần khai báo lại restriction;
 * cả hai gắn @CascadeSoftDelete để kế hoạch bị ẩn cùng thiết bị / danh mục vật tư.
 *
 * LƯU Ý (sau merge): lubrication_history KHÔNG còn FK trỏ tới bảng này — nó tham
 * chiếu trực tiếp tới equipment_id (xem {@link LubricationHistory}).
 */
@Entity
@Table(name = "lubrication_plans")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false, of = "id")
public class LubricationPlan extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    @CascadeSoftDelete
    private Equipment equipment;

    /** Chu kỳ bảo dưỡng tính theo tháng */
    @Column(name = "cycle_months")
    private Integer cycleMonths;

    /** Ngày đến hạn bảo dưỡng tiếp theo (dùng để gửi thông báo trước 3 ngày) */
    @Column(name = "next_due_date")
    private LocalDate nextDueDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private LubricationStatus status = LubricationStatus.NOT_LUBRICATED;

    /** Liên kết vật tư tiêu hao (dầu/mỡ) trong danh mục kho */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumable_id")
    @CascadeSoftDelete
    private Consumable consumable;

    @Column(precision = 10, scale = 2)
    private BigDecimal quantity;
}
