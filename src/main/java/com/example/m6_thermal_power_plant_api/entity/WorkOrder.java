package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
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
 * RepairRequest / Account đều đã @SQLRestriction("is_deleted = false") nên
 * các quan hệ dưới đây KHÔNG cần khai báo lại restriction — và vẫn giữ LAZY
 * bình thường (không cần ép EAGER như cách làm với @SoftDelete).
 */
@Entity
@Table(name = "work_orders")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@ToString(callSuper = true, exclude = {"members", "extensions", "sparePartsIssues", "consumableIssues", "technicalAssessments"})
@EqualsAndHashCode(callSuper = false, of = "id")
public class WorkOrder extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "order_code", unique = true, nullable = false, length = 50)
    private String orderCode;

    /** Quan hệ 1-1: mỗi PCT chỉ từ 1 yêu cầu duy nhất */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repair_request_id", unique = true)
    private RepairRequest repairRequest;

    /** Người lãnh đạo công việc */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id")
    private Account leader;

    /** Chỉ huy trực tiếp */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "direct_supervisor_id")
    private Account directSupervisor;

    /** Người giám sát an toàn */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "safety_supervisor_id")
    private Account safetySupervisor;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(length = 100)
    private String status;

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

    @JsonIgnore
    @OneToMany(mappedBy = "workOrder", fetch = FetchType.LAZY)
    private List<TechnicalAssessment> technicalAssessments;
}
