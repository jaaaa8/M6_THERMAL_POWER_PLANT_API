package com.example.m6_thermal_power_plant_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
// Cân nhắc bật soft delete nếu nghiệp vụ cho phép Trưởng Ca/Kíp tự huỷ
// request khi CHƯA có WorkOrder liên kết. Nếu bật, thêm:
// import org.hibernate.annotations.SoftDelete;
// import org.hibernate.annotations.SoftDeleteType;

import java.time.LocalDateTime;

/**
 * Phiếu yêu cầu sửa chữa — do Trưởng Ca/Kíp tạo khi thiết bị hỏng.
 * Table: repair_requests
 *
 * Soft delete: CHƯA bật mặc định. User Story #35 cho phép "xoá request",
 * nhưng nếu request đã có WorkOrder (quan hệ 1-1) thì hard-delete sẽ phá
 * tham chiếu. Nếu muốn bật, thêm:
 *   {@code @SoftDelete(columnName = "is_deleted", strategy = SoftDeleteType.DELETED)}
 * ngay trên class, và ở tầng service chỉ cho phép xoá khi workOrder == null.
 */
@Entity
@Table(name = "repair_requests")
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = "workOrder")
@EqualsAndHashCode(of = "id")
public class RepairRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "request_code", unique = true, nullable = false, length = 50)
    private String requestCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @SQLRestriction("is_deleted = false")
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    /** Trưởng Ca / Trưởng Kíp tạo yêu cầu (đăng nhập bằng tài khoản) */
    @ManyToOne(fetch = FetchType.LAZY)
    @SQLRestriction("is_deleted = false")
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
    @OneToOne(mappedBy = "repairRequest")
    private WorkOrder workOrder;
}
