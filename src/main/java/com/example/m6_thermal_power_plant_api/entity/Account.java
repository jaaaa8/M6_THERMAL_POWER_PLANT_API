package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.example.m6_thermal_power_plant_api.entity.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;

/**
 * Tài khoản đăng nhập — là "actor" thực hiện hầu hết hành vi nghiệp vụ
 * trong hệ thống (tạo request, ký phiếu công tác, cấp vật tư, mượn CCDC...).
 * Table: accounts
 * Join table account_roles được quản lý tại đây bằng @ManyToMany.
 *
 * Có 2 cờ trạng thái phục vụ 2 mục đích khác nhau, KHÔNG thay thế nhau:
 * - is_active : khoá/mở tài khoản tạm thời (vẫn tồn tại, chỉ không đăng nhập được)
 * - is_deleted: xoá tài khoản (xem {@link BaseSoftDeleteEntity})
 */
@Entity
@Table(name = "accounts")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@ToString(callSuper = true, exclude = "roles")
@EqualsAndHashCode(callSuper = false, of = "id")
public class Account extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Employee cũng @SQLRestriction("is_deleted = false") nên không cần khai
     *  báo lại restriction ở đây — mỗi tài khoản thuộc về đúng 1 nhân viên. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", unique = true)
    private Employee employee;

    // composite voi cot active_flag de tao unique sau khi run sql script o thu muc db
    @Column(nullable = false, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /** Trạng thái tài khoản (KHÁC với is_deleted) */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private AccountStatus status = AccountStatus.ACTIVE;

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
