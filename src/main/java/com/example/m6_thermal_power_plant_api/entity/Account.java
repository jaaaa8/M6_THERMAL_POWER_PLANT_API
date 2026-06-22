package com.example.m6_thermal_power_plant_api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import java.util.List;

/**
 * Tài khoản đăng nhập — là "actor" thực hiện hầu hết hành vi nghiệp vụ
 * trong hệ thống (tạo request, ký phiếu công tác, cấp vật tư, mượn CCDC...).
 * Table: accounts
 * Join table account_roles được quản lý tại đây bằng @ManyToMany.

 * Có 2 cờ trạng thái phục vụ 2 mục đích khác nhau, không thay thế nhau:
 * - is_active : khoá/mở tài khoản tạm thời (vẫn còn tồn tại, chỉ không đăng nhập được)
 * - is_deleted: xoá tài khoản (do Hibernate @SoftDelete tự quản lý)
 */
@Entity
@Table(name = "accounts")
@SoftDelete(columnName = "is_deleted", strategy = SoftDeleteType.DELETED)
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = "roles")
@EqualsAndHashCode(of = "id")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Mỗi tài khoản thuộc về đúng 1 nhân viên */
    @OneToOne(fetch = FetchType.LAZY)
    @SQLRestriction("is_deleted = false")
    @JoinColumn(name = "employee_id", unique = true)
    private Employee employee;

    @Column(unique = true, nullable = false, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /** Khoá/mở tài khoản tạm thời (KHÁC với is_deleted) */
    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    /**
     * Phân quyền.
     * Join table: account_roles (account_id, role_id)
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "account_roles",
        joinColumns        = @JoinColumn(name = "account_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles;
}
