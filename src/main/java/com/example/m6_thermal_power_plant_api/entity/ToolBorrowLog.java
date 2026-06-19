package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.enums.BorrowType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Nhật ký mượn / trả công cụ dụng cụ.
 * Table: tool_borrow_logs
 *
 * Không áp dụng @SoftDelete: là nhật ký giao dịch mượn/trả, không xoá.
 */
@Entity
@Table(name = "tool_borrow_logs")
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ToolBorrowLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tool_id")
    private Tool tool;

    /** Người mượn/trả (đăng nhập bằng tài khoản) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id")
    private Account account;

    @Column
    private Integer quantity;

    /** BORROW = mượn | RETURN = trả (cột DB là VARCHAR(50) tự do) */
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 50)
    private BorrowType transactionType;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;
}
