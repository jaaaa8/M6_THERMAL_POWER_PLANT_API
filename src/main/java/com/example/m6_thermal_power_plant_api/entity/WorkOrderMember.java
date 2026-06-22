package com.example.m6_thermal_power_plant_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Nhân viên tham gia Phiếu Công Tác.
 * Table: work_order_members
 *
 * Không soft-delete: dữ liệu phụ thuộc WorkOrder, dùng left_at để đánh dấu
 * thời điểm rời khỏi công việc thay vì xoá dòng. Account đã @SQLRestriction
 * nên không cần khai báo lại restriction ở quan hệ account.
 */
@Entity
@Table(name = "work_order_members")
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class WorkOrderMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id")
    private WorkOrder workOrder;

    /** Thành viên tham gia (đăng nhập bằng tài khoản) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    /** Vai trò trong công việc (VD: Thợ vận hành, Thợ điện...) */
    @Column(name = "role_in_task", length = 255)
    private String roleInTask;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;
}
