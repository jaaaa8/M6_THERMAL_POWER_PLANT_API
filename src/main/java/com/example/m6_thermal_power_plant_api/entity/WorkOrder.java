package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.example.m6_thermal_power_plant_api.entity.base.CascadeSoftDelete;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Phiếu Công Tác (PCT) — do Quản đốc/Tổ trưởng sửa chữa tạo từ yêu cầu sửa chữa.
 * Table: work_orders
 *
 * Không soft-delete: là chứng từ pháp lý (các bên ký trước khi sửa chữa).
 * Khi huỷ, cập nhật cột status (VD: "CANCELLED") thay vì ẩn dòng — xem
 * {@code WorkOrdersDeletionException} ở package exception, vốn đã được tạo
 * sẵn cho đúng mục đích chặn hard-delete này ở tầng service.
 *
 * RepairRequest / Employee đều đã @SQLRestriction("is_deleted = false") nên
 * các quan hệ dưới đây KHÔNG cần khai báo lại restriction — và vẫn giữ LAZY
 * bình thường (không cần ép EAGER như cách làm với @SoftDelete).
 *
 * leader / directSupervisor / safetySupervisor trỏ tới Employee (KHÔNG phải
 * Account): Employee là bảng gốc, không phải nhân viên nào cũng có tài khoản
 * đăng nhập, nhưng vẫn phải chọn được họ vào các vai trò này.
 */
@Entity
@Table(name = "work_orders")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@ToString(callSuper = true, exclude = {"members", "extensions", "sparePartsIssues", "consumableIssues", "workOrderEquipments"})
@EqualsAndHashCode(callSuper = false, of = "id")
public class WorkOrder extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // composite voi cot active_flag de tao unique sau khi run sql script o thu muc db
    @Column(name = "order_code", nullable = false, length = 50)
    private String orderCode;

    /** Quan hệ n-1: mỗi PCT thuộc về 1 yêu cầu. 1 yêu cầu có thể có nhiều PCT */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repair_request_id")
    @CascadeSoftDelete
    private RepairRequest repairRequest;

    /** Người lãnh đạo công việc */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id")
    private Employee leader;

    /** Chỉ huy trực tiếp */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "direct_supervisor_id")
    private Employee directSupervisor;

    /** Người giám sát an toàn */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "safety_supervisor_id")
    private Employee safetySupervisor;

    /** Thiết bị trong phạm vi phiếu — WO thủ công nhiều thiết bị (không có RepairRequest).
     *  Mỗi dòng mang trạng thái làm việc RIÊNG (IN_PROGRESS/COMPLETED/CANCELED).
     *  WO từ yêu cầu: danh sách này rỗng, thiết bị lấy qua repairRequest. */
    @JsonIgnore
    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL)
    private List<WorkOrderEquipment> workOrderEquipments;

    /** Danh sách Equipment thuần — tương thích ngược cho PDF và lịch sử sửa chữa. */
    public List<Equipment> getEquipments() {
        if (workOrderEquipments == null) return List.of();
        return workOrderEquipments.stream().map(WorkOrderEquipment::getEquipment).toList();
    }

    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * Thời điểm kết thúc THỰC TẾ — null suốt đời phiếu, chỉ được đóng dấu khi
     * phiếu chuyển COMPLETED (xem MaintenanceService#completeWorkOrder). KHÔNG
     * phải "dự kiến kết thúc": mốc dự kiến nhập lúc tạo không bao giờ đúng nên
     * đã bỏ (V13).
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "repair_description")
    private String repairDescription;

    /** Mới tạo (OPEN) / Đang thực hiện / Hoàn thành / Đã huỷ */
    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private WorkOrderStatus status;

    /** Loại phiếu: sửa chữa (mặc định) hoặc bảo dưỡng dầu mỡ theo kế hoạch */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private WorkOrderType type = WorkOrderType.REPAIR;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** Người cấp phiếu — tài khoản đăng nhập đã tạo PCT (in lên bản PDF).
     *  Account đã @SQLRestriction nên không cần khai báo lại restriction. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Account createdBy;

    /** Đường dẫn file PDF phiếu công tác đã xuất */
    @Column(name = "pdf_path", length = 500)
    private String pdfPath;

    @JsonIgnore
    @OneToMany(mappedBy = "workOrder", fetch = FetchType.LAZY)
    private List<WorkOrderMember> members;

    @JsonIgnore
    @OneToMany(mappedBy = "workOrder", fetch = FetchType.LAZY)
    private List<WorkOrderExtension> extensions;

    @JsonIgnore
    @OneToMany(mappedBy = "workOrder", fetch = FetchType.LAZY)
    private List<SparePartsIssue> sparePartsIssues;

    @JsonIgnore
    @OneToMany(mappedBy = "workOrder", fetch = FetchType.LAZY)
    private List<ConsumableIssue> consumableIssues;

}
