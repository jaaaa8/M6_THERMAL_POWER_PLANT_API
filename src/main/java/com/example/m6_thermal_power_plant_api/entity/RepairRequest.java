package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.example.m6_thermal_power_plant_api.entity.base.CascadeSoftDelete;
import com.example.m6_thermal_power_plant_api.entity.enums.RepairPriority;
import com.example.m6_thermal_power_plant_api.entity.enums.RepairRequestStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Phiếu yêu cầu sửa chữa — do Trưởng Ca/Kíp tạo khi thiết bị hỏng.
 * Table: repair_requests
 *
 * Soft delete: ĐÃ BẬT (xem {@link BaseSoftDeleteEntity}), đáp ứng User Story
 * #35 ("Tôi có thể tạo mới, xoá một yêu cầu sửa chữa 1 thiết bị"). An toàn vì
 * dùng @SQLRestriction (không như @SoftDelete, không cấm LAZY).
 *
 * MỨC ĐỘ AN TOÀN SOFT-DELETE: ⚠️ CÓ ĐIỀU KIỆN
 *
 * RÀNG BUỘC NGHIỆP VỤ BẮT BUỘC ở tầng service: CHỈ cho phép soft-delete khi
 * {@code workOrders == null || workOrders.isEmpty()}. Lý do:
 *  - WorkOrder không soft-delete (chứng từ pháp lý — xem {@link WorkOrder}).
 *  - Nếu Request bị ẩn mà WorkOrder vẫn còn → gọi
 *    workOrder.getRepairRequest() sẽ ném ObjectNotFoundException (bị
 *    @SQLRestriction lọc).
 *  - Nếu cố cascade qua WorkOrder thì lại vi phạm rule "PCT không xoá".
 *
 * Khuyến nghị implement ở service: nếu user cố xoá request đã có PCT, ném
 * exception nghiệp vụ rõ ràng (vd: "Không thể xoá yêu cầu đã có PCT —
 * vui lòng huỷ PCT trước bằng cách chuyển trạng thái CANCELLED").
 */
@Entity
@Table(name = "repair_requests")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@ToString(callSuper = true, exclude = "workOrders")
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

    /** Mức độ ưu tiên: HIGH | LOW */
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private RepairPriority priority;

    /** Đang chờ xử lý / Đã duyệt / Đang xử lý / Hoàn thành */
    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private RepairRequestStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** 1 yêu cầu có thể sinh ra nhiều phiếu công tác */
    @JsonIgnore
    @OneToMany(mappedBy = "repairRequest", fetch = FetchType.LAZY)
    private List<WorkOrder> workOrders;
}
