package com.example.m6_thermal_power_plant_api.entity;

import jakarta.persistence.*;
import lombok.*;

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
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class WorkOrderExtension {

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
