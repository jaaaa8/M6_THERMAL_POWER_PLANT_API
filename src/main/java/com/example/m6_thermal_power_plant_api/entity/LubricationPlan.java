package com.example.m6_thermal_power_plant_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Kế hoạch bảo dưỡng dầu mỡ cho thiết bị.
 * Table: lubrication_plans
 *
 * CHỦ ĐỘNG KHÔNG soft-delete: lubrication_history có FK trỏ tới bảng này mà
 * KHÔNG có business rule "chỉ xoá khi hết lịch sử" giống RepairRequest —
 * nếu soft-delete plan mà vẫn còn history, gọi history.getPlan() sẽ ném
 * ObjectNotFoundException (bị lọc bởi is_deleted = false), làm hỏng chức
 * năng xem lại lịch sử bảo dưỡng cũ. Nếu sau này cần "ngừng" 1 kế hoạch,
 * nên thêm cột status riêng (VD: ACTIVE/STOPPED) thay vì xoá mềm.
 *
 * Equipment / Consumable đều đã @SQLRestriction nên 2 quan hệ dưới đây
 * KHÔNG cần khai báo lại restriction.
 */
@Entity
@Table(name = "lubrication_plans")
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = "history")
@EqualsAndHashCode(of = "id")
public class LubricationPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    /** Chu kỳ bảo dưỡng tính theo tháng */
    @Column(name = "cycle_months")
    private Integer cycleMonths;

    /** Ngày đến hạn bảo dưỡng tiếp theo (dùng để gửi thông báo trước 3 ngày) */
    @Column(name = "next_due_date")
    private LocalDate nextDueDate;

    @Column(name = "lubricant_type", length = 255)
    private String lubricantType;

    /** Liên kết vật tư tiêu hao (dầu/mỡ) trong danh mục kho */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumable_id")
    private Consumable consumable;

    @Column(precision = 10, scale = 2)
    private BigDecimal quantity;

    @JsonIgnore
    @OneToMany(mappedBy = "plan", fetch = FetchType.LAZY)
    private List<LubricationHistory> history;
}
