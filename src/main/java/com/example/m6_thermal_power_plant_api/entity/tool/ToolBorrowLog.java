package com.example.m6_thermal_power_plant_api.entity.tool;

import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.enums.BorrowStatus;
import jakarta.persistence.*;
import lombok.*;

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
    @JoinColumn(name = "tool_id", nullable = false)
    private Tool tool;

    /** Người mượn */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /** Số lượng mượn (gốc, không đổi khi trả từng phần) */
    @Column(nullable = false)
    private Integer quantity;

    /** Số lượng đã trả tích lũy — cho phép trả nhiều lần, mỗi lần một ít */
    @Builder.Default
    @Column(name = "returned_quantity", nullable = false)
    private Integer returnedQuantity = 0;

    /** Lý do mượn */
    @Column(name = "borrow_purpose", columnDefinition = "TEXT")
    private String borrowPurpose;

    /** PENDING / APPROVED / REJECTED / RETURNED */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BorrowStatus status = BorrowStatus.PENDING;

    /** Ngày nhân sự đăng ký mượn (tạo phiếu) */
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    /** Ngày thủ kho xác nhận giao công cụ thực tế (khác ngày tạo phiếu) */
    @Column(name = "delivered_date")
    private LocalDateTime deliveredDate;

    /** Hạn phải trả */
    @Column(name = "due_date")
    private LocalDateTime dueDate;

    /** Ngày trả thực tế */
    @Column(name = "actual_return_date")
    private LocalDateTime actualReturnDate;

    /** Ghi chú tình trạng công cụ khi nhận trả (phục vụ phát hiện hư hỏng) */
    @Column(name = "return_note", columnDefinition = "TEXT")
    private String returnNote;

    /** Thủ kho duyệt / xác nhận giao */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Account approvedBy;

    /** Đã gửi email nhắc quá hạn hay chưa — tránh job gửi lặp nhiều lần */
    @Builder.Default
    @Column(name = "overdue_notified", nullable = false)
    private Boolean overdueNotified = false;

    /** Đã gửi email nhắc sắp đến hạn (trước 1 ngày) hay chưa */
    @Builder.Default
    @Column(name = "due_soon_notified", nullable = false)
    private Boolean dueSoonNotified = false;
}