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
 *  - status (AccountStatus.ACTIVE/LOCKED): khoá/mở đăng nhập tạm thời —
 *    bản ghi vẫn tồn tại, mọi tham chiếu LAZY vẫn đọc được, chỉ là không
 *    đăng nhập được.
 *  - is_deleted: xoá tài khoản (xem {@link BaseSoftDeleteEntity}).
 *
 * MỨC ĐỘ AN TOÀN SOFT-DELETE: ✅ AN TOÀN VỀ CASCADE
 *  - Không có @CascadeSoftDelete nào trỏ về Account → soft-delete Account
 *    KHÔNG kéo theo bảng nào (kể cả Employee — đúng nghiệp vụ).
 *  - Hậu quả gián tiếp đáng kể: mọi chứng từ FK tới accounts(id) (xem danh
 *    sách ở Employee Javadoc) sẽ bị "đứt" proxy do @SQLRestriction lọc khi
 *    LAZY-load → ObjectNotFoundException.
 *  - KHUYẾN NGHỊ: nếu chỉ muốn KHOÁ ĐĂNG NHẬP tạm thời (nghỉ phép, mất pass,
 *    đổi bộ phận...) → dùng {@code status = LOCKED}. KHÔNG soft-delete.
 *    Soft-delete chỉ dùng khi tài khoản bị huỷ hẳn và đã chấp nhận việc các
 *    chứng từ cũ mất khả năng resolve proxy "createdBy/issuedBy/...".
 */
@Entity
@Table(name = "accounts")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = "roles")
@EqualsAndHashCode(callSuper = false, of = "id")
public class Account extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Employee cũng @SQLRestriction("is_deleted = false") nên không cần khai
     * báo lại restriction ở đây — mỗi tài khoản thuộc về đúng 1 nhân viên.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", unique = true)
    private Employee employee;

    @Column(unique = true, nullable = false, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(unique = true, length = 255)
    private String email;

    /** Khoá/mở tài khoản tạm thời (KHÁC với is_deleted) */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
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
