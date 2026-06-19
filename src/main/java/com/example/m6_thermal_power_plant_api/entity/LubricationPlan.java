package com.example.m6_thermal_power_plant_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
// Cân nhắc bật soft delete nếu nghiệp vụ cho phép huỷ kế hoạch dầu mỡ mà
// KHÔNG cascade xoá lịch sử (LubricationHistory) đã thực hiện. Nếu bật, thêm:
// import org.hibernate.annotations.SoftDelete;
// import org.hibernate.annotations.SoftDeleteType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Kế hoạch bảo dưỡng dầu mỡ cho thiết bị.
 * Table: lubrication_plans
 *
 * Soft delete: CHƯA bật mặc định — xem ghi chú phía trên class. Nếu bật,
 * KHÔNG cascade xoá LubricationHistory đã thực hiện (lịch sử bảo dưỡng cần
 * giữ nguyên vì thiết bị đã thực sự được bảo dưỡng theo kế hoạch đó).
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

    @ManyToOne(fetch = FetchType.EAGER)
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
    @SQLRestriction("is_deleted = false")
    @JoinColumn(name = "consumable_id")
    private Consumable consumable;

    @Column(precision = 10, scale = 2)
    private BigDecimal quantity;

    @JsonIgnore
    @OneToMany(mappedBy = "plan", fetch = FetchType.LAZY)
    private List<LubricationHistory> history;
}
