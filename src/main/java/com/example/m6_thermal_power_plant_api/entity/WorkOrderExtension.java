package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.example.m6_thermal_power_plant_api.entity.base.CascadeSoftDelete;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
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
    @CascadeSoftDelete
    private WorkOrder workOrder;

    @Column(columnDefinition = "TEXT")
    private String reason;

    /** Người phê duyệt gia hạn (đăng nhập bằng tài khoản) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Account approvedBy;

    /**
     * Thời điểm Tổ trưởng tạo dòng gia hạn và gửi Trưởng ca (phiếu đang STOPPED
     * chuyển sang WAITING_FOR_APPROVAL) — in vào cột "Thời gian tạm hoãn" trên
     * bản PDF. NULL với dữ liệu trước V12.
     */
    @CreationTimestamp
    @Column(name = "requested_at", updatable = false)
    private LocalDateTime requestedAt;

    /**
     * NGÀY Trưởng ca cho phép đơn vị công tác làm tiếp — null tới khi duyệt.
     * Mỗi lần gia hạn chỉ kéo dài 1 ngày; Tổ trưởng mặc định xin "ngày mai"
     * nhưng Trưởng ca mới là người chốt ngày (có thể lùi xa hơn nếu chưa cô lập
     * được thiết bị), nên chỉ ngày ĐƯỢC DUYỆT mới đáng lưu (V13).
     */
    @Column(name = "allowed_date")
    private LocalDate allowedDate;
}
