package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.enums.BorrowStatus;
import com.example.m6_thermal_power_plant_api.entity.enums.BorrowType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "tool_borrow_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ToolBorrowLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Công cụ được mượn */
    @ManyToOne(fetch = FetchType.LAZY)
    @SQLRestriction("is_deleted = false")
    @JoinColumn(name = "tool_id", nullable = false)
    private Tool tool;

    /** Người mượn */
    @ManyToOne(fetch = FetchType.LAZY)
    @SQLRestriction("is_deleted = false")
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
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BorrowStatus status;

    /** Ngày tạo phiếu */
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    /** Hạn phải trả */
    @Column(name = "due_date")
    private LocalDateTime dueDate;

    /** Ngày trả thực tế */
    @Column(name = "actual_return_date")
    private LocalDateTime actualReturnDate;

    /** Thủ kho duyệt phiếu */
    @ManyToOne(fetch = FetchType.LAZY)
    @SQLRestriction("is_deleted = false")
    @JoinColumn(name = "approved_by")
    private Account approvedBy;
}