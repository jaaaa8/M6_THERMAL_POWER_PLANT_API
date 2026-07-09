package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.example.m6_thermal_power_plant_api.entity.base.CascadeSoftDelete;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * MỘT LẦN cấp vật tư cho Phiếu Công Tác (thực thể CHA của phiếu vật tư thay thế
 * + phiếu vật tư tiêu hao tạo trong cùng một hành động/transaction).
 * Table: supplies_issues (V9)
 *
 * Trước V9 hai bảng con (spare_parts_issues / consumable_issues) không có gì
 * nối với nhau — bảng cha này cho phép đánh số "lần cấp #N" của một PCT và
 * xuất PDF theo TỪNG lần cấp. Dữ liệu vật tư vẫn nằm nguyên ở 2 bảng con.
 *
 * MỨC ĐỘ AN TOÀN SOFT-DELETE: ❌ KHÔNG NÊN — chứng từ, cùng lý do với
 * {@link SparePartsIssue}/{@link ConsumableIssue}; chỉ tồn tại đường cascade
 * từ WorkOrder (bản thân WorkOrder cũng không bao giờ bị xoá trong nghiệp vụ).
 */
@Entity
@Table(name = "supplies_issues")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "id")
public class SuppliesIssue extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id")
    @CascadeSoftDelete
    private WorkOrder workOrder;

    /** Người thực hiện cấp phát (đăng nhập bằng tài khoản) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_by")
    private Account issuedBy;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;
}
