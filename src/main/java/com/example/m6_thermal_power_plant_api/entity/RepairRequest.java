package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.example.m6_thermal_power_plant_api.entity.base.CascadeSoftDelete;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * Phiếu yêu cầu sửa chữa — do Trưởng Ca/Kíp tạo khi thiết bị hỏng.
 * Table: repair_requests
 *
 * Soft delete: ĐÃ BẬT (xem {@link BaseSoftDeleteEntity}), đáp ứng User Story
 * #35 ("Tôi có thể tạo mới, xoá một yêu cầu sửa chữa 1 thiết bị"). An toàn vì
 * dùng @SQLRestriction (không như @SoftDelete, không cấm LAZY).
 *
 * RÀNG BUỘC NGHIỆP VỤ BẮT BUỘC ở tầng service: CHỈ cho phép soft-delete khi
 * {@code getWorkOrder() == null}. Vì work_orders.repair_request_id là FK trỏ
 * tới bảng này (quan hệ 1-1) — nếu đã có WorkOrder mà vẫn xoá mềm request,
 * gọi workOrder.getRepairRequest() sau đó sẽ ném ObjectNotFoundException
 * (bị lọc bởi is_deleted = false).
 */
@Entity
@Table(name = "repair_requests")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@ToString(callSuper = true, exclude = "workOrder")
@EqualsAndHashCode(callSuper = false, of = "id")
public class RepairRequest extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // composite voi cot active_flag de tao unique sau khi run sql script o thu muc db
    @Column(name = "request_code", nullable = false, length = 50)
    private String requestCode;

    /** Equipment đã @SQLRestriction nên không cần khai báo lại ở đây. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    @CascadeSoftDelete
    private Equipment equipment;

    /** Trưởng Ca / Trưởng Kíp tạo yêu cầu (đăng nhập bằng tài khoản).
     *  Account đã @SQLRestriction nên không cần khai báo lại ở đây. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    private Account requester;

    @Column(name = "incident_description", columnDefinition = "TEXT")
    private String incidentDescription;

    /** Giá trị hợp lệ theo DB: 'high' | 'low' (MySQL ENUM, so khớp không phân biệt hoa/thường) */
    @Column(length = 50)
    private String priority;

    /** Đang chờ xử lý / Đang xử lý / Hoàn thành */
    @Column(length = 100)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** 1 yêu cầu sinh ra đúng 1 phiếu công tác */
    @JsonIgnore
    @OneToOne(mappedBy = "repairRequest", fetch = FetchType.LAZY)
    private WorkOrder workOrder;
}
