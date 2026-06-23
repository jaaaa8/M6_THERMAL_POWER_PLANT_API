package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.example.m6_thermal_power_plant_api.entity.enums.BorrowStatus;
import com.example.m6_thermal_power_plant_api.entity.enums.BorrowType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * Nhật ký mượn / trả công cụ dụng cụ.
 * Table: tool_borrow_logs
 *
 * Không soft-delete: là nhật ký giao dịch mượn/trả, không xoá — dùng field
 * {@code status} để theo dõi tiến trình duyệt/trả. Tool / Account đều đã
 * @SQLRestriction nên không cần khai báo lại restriction ở các quan hệ dưới.
 */
@Entity
@Table(name = "tool_borrow_logs")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "id")
public class ToolBorrowLog extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Công cụ được mượn */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tool_id", nullable = false)
    private Tool tool;

    /** Người mượn (đăng nhập bằng tài khoản) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /** Số lượng mượn */
    @Column(nullable = false)
    private Integer quantity;

    /** BORROW | RETURN */
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 50)
    private BorrowType transactionType;

    /** PENDING | APPROVED | REJECTED | RETURNED */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BorrowStatus status = BorrowStatus.PENDING;

    /** Ngày tạo phiếu */
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    /** Hạn phải trả — phục vụ User Story #32 (tự động email khi quá hạn) */
    @Column(name = "due_date")
    private LocalDateTime dueDate;

    /** Ngày trả thực tế */
    @Column(name = "actual_return_date")
    private LocalDateTime actualReturnDate;

    /** Thủ kho duyệt phiếu (đăng nhập bằng tài khoản) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Account approvedBy;
}
