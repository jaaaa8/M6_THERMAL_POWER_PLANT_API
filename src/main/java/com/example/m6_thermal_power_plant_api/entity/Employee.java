package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.example.m6_thermal_power_plant_api.entity.base.CascadeSoftDelete;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

/**
 * Nhân sự (hồ sơ nhân viên — KHÔNG phải tài khoản đăng nhập).
 * Table: employees
 *
 * Lưu ý: hầu hết bảng nghiệp vụ (repair_requests, work_orders,
 * technical_assessments, tool_borrow_logs...) tham chiếu accounts(id),
 * KHÔNG tham chiếu trực tiếp employees(id). Employee chỉ liên kết qua
 * accounts.employee_id (1 nhân viên ↔ 1 tài khoản). Không phải nhân viên
 * nào cũng có tài khoản — chỉ cấp cho người cần dùng hệ thống.
 *
 * MỨC ĐỘ AN TOÀN SOFT-DELETE: ⚠️ CẨN THẬN
 *  - Soft-delete Employee sẽ CASCADE xuống Account (xem @CascadeSoftDelete
 *    ở Account.employee). Account bị ẩn → mọi tham chiếu "sống" tới account
 *    đó (RepairRequest.requester, WorkOrder.leader/directSupervisor/
 *    safetySupervisor, WorkOrderMember.account, *Issue.issuedBy,
 *    *Receipt.receivedBy, *Inventory.account, WorkOrderExtension.approvedBy,
 *    ToolBorrowLog.account, TechnicalAssessment.assessor) sẽ ném
 *    ObjectNotFoundException khi LAZY-load proxy.
 *  - CHỈ soft-delete khi: (1) nhân viên thật sự nghỉ việc, VÀ (2) đã xử lý
 *    xong mọi chứng từ "đang dở" do tài khoản của họ đứng tên.
 *  - Chiều ngược lại an toàn: xoá Account KHÔNG đụng tới Employee (đúng
 *    nghiệp vụ — thu hồi quyền truy cập không đồng nghĩa với mất hồ sơ).
 *
 * Soft delete: xem {@link BaseSoftDeleteEntity}.
 */
@Entity
@Table(name = "employees")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@ToString(callSuper = true, exclude = "account")
@EqualsAndHashCode(callSuper = false, of = "id")
public class Employee extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Mã nhân viên */
    // composite voi cot active_flag de tao unique sau khi run sql script o thu muc db
    @Column(name = "employee_code", nullable = false, length = 50)
    private String employeeCode;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(nullable = false, length = 255)
    private String gmail;

    @Column(length = 12)
    private String phone;

    /** Department cũng @SQLRestriction("is_deleted = false") nên quan hệ này
     *  tự động ẩn phòng ban đã xoá mềm — không cần khai báo lại restriction ở đây. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @CascadeSoftDelete
    private Department department;

    /** Chức vụ */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    @CascadeSoftDelete
    private Position position;

    /** Chuyên môn */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expertise_id")
    @CascadeSoftDelete
    private Expertise expertise;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    /** Đường dẫn file ảnh đính kèm */
    @Column(name = "img_path", columnDefinition = "TEXT")
    private String imgPath;

    /** Tài khoản đăng nhập (null nếu nhân viên chưa được cấp tài khoản) */
    @JsonIgnore
    @OneToOne(mappedBy = "employee", fetch = FetchType.LAZY)
    private Account account;
}
