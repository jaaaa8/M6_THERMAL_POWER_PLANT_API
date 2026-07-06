package com.example.m6_thermal_power_plant_api.dto.spare_parts;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SparePartReceiptCreateDTO {
    @Size(max = 50, message = "Mã hóa đơn không được vượt quá 50 ký tự")
    private String receiptCode;

    @NotNull(message = "Vui lòng chọn vật tư tiêu hao")
    private Integer sparePartId;

    @NotNull(message = "Số lượng nhập không được để trống")
    @DecimalMin(value = "0.01", message = "Số lượng nhập phải lớn hơn 0")
    private BigDecimal quantity;

    @Size(max = 255, message = "Tên nhà cung cấp không được vượt quá 255 ký tự")
    private String supplier;

    private LocalDateTime receivedAt;
}
