package com.example.m6_thermal_power_plant_api.dto.tool;


import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ToolBorrowRequest {

    @NotNull(message = "Công cụ không được để trống")
    private Integer toolId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng mượn phải lớn hơn 0")
    private Integer quantity;

    private String borrowPurpose;

    @NotNull(message = "Hạn trả không được để trống")
    @Future(message = "Hạn trả phải lớn hơn thời điểm hiện tại")
    private LocalDateTime dueDate;
}
