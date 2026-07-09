package com.example.m6_thermal_power_plant_api.dto.tool;


import com.example.m6_thermal_power_plant_api.entity.enums.BorrowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolBorrowLogResponse {
    private Integer id;

    private Integer toolId;
    private String toolCode;
    private String toolName;

    private Integer accountId;
    private String accountName;

    private Integer quantity;
    private Integer returnedQuantity;
    private String borrowPurpose;
    private BorrowStatus status;

    private LocalDateTime transactionDate;
    private LocalDateTime deliveredDate;
    private LocalDateTime dueDate;
    private LocalDateTime actualReturnDate;

    private String returnNote;

    private Integer approvedById;
    private String approvedByName;

    private Boolean overdueNotified;

    /** Cờ tiện ích cho FE: true nếu đã quá hạn mà chưa trả */
    private boolean overdue;
}
