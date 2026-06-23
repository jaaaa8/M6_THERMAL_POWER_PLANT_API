package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * Gia hạn Phiếu Công Tác.
 * Table: work_order_extensions
 *
 * Không soft-delete: là lịch sử gia hạn, không xoá. Account đã @SQLRestriction
 * nên không cần khai báo lại restriction ở quan hệ approvedBy.
 */
@Entity
@Table(name = "work_order_extensions")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "id")
public class WorkOrderExtension extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id")
    private WorkOrder workOrder;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "extended_until")
    private LocalDateTime extendedUntil;

    /** Người phê duyệt gia hạn (đăng nhập bằng tài khoản) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Account approvedBy;
}
