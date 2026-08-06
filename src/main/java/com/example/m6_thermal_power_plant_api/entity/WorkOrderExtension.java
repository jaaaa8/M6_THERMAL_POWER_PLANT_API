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
 * NHẬT KÝ CÔNG TÁC HÀNG NGÀY của Phiếu Công Tác — mỗi dòng là MỘT ngày.
 * Table: work_order_extensions (giữ tên cũ; trước V20 bảng này là "đơn xin gia hạn").
 *
 * Dòng được tạo lúc Trưởng ca "mở phiếu ngày" và đóng lại lúc "khoá phiếu ngày".
 * Đây là nguồn dữ liệu cho mục "Cho phép làm việc và kết thúc công tác hàng ngày"
 * trên bản in PCT, và cho luật huỷ phiếu (chưa có dòng nào = chưa chạy ngày nào).
 *
 * Không soft-delete: là lịch sử công tác, không xoá.
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

    /** Ghi chú lúc khoá phiếu ngày (tuỳ chọn) — in vào cột lý do trên bản PDF. */
    @Column(columnDefinition = "TEXT")
    private String reason;

    /**
     * Giờ MỞ phiếu ngày. Tự điền lúc insert nên luôn khớp thời điểm Trưởng ca bấm mở.
     * NULL với dữ liệu trước V12.
     */
    @CreationTimestamp
    @Column(name = "requested_at", updatable = false)
    private LocalDateTime requestedAt;

    /** Giờ KHOÁ phiếu ngày — null nghĩa là ngày công tác đang mở (V20). */
    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    /**
     * NGÀY công tác của dòng này. Với dữ liệu gia hạn cũ (trước V20) đây là ngày
     * Trưởng ca cho phép làm tiếp — cùng ý nghĩa nên đọc lại được.
     */
    @Column(name = "allowed_date")
    private LocalDate allowedDate;
}
