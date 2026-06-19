package com.example.m6_thermal_power_plant_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

/**
 * Nhân sự (hồ sơ nhân viên — KHÔNG phải tài khoản đăng nhập).
 * Table: employees
 *
 * Lưu ý: trong schema hiện tại, hầu hết các bảng nghiệp vụ (repair_requests,
 * work_orders, technical_assessments, tool_borrow_logs...) đều tham chiếu tới
 * accounts(id), KHÔNG tham chiếu trực tiếp employees(id). Employee chỉ được
 * liên kết qua accounts.employee_id (1 nhân viên ↔ 1 tài khoản).
 *
 * Soft delete: is_deleted do Hibernate quản lý.
 */
@Entity
@Table(name = "employees")
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = "account")
@EqualsAndHashCode(of = "id")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Mã nhân viên */
    @Column(name = "employee_code", unique = true, nullable = false, length = 50)
    private String employeeCode;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(nullable = false, length = 255)
    private String gmail;

    @Column(length = 12)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @SQLRestriction("is_deleted = false")
    @JoinColumn(name = "department_id")
    private Department department;

    /** Chức vụ */
    @Column(length = 255)
    private String position;

    /** Chuyên môn */
    @Column(length = 255)
    private String expertise;

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
