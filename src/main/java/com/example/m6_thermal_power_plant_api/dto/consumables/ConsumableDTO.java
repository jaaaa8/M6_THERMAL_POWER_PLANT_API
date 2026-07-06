package com.example.m6_thermal_power_plant_api.dto.consumables;

import com.example.m6_thermal_power_plant_api.entity.enums.PartStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumableDTO {

    private Integer id;

    @Size(max = 30, message = "Mã vật tư tiêu hao không được vượt quá 30 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_-]*$", message = "Mã vật tư tiêu hao chỉ được chứa chữ cái, số, dấu gạch ngang (-) và gạch dưới (_)")
    private String consumableCode;

    @NotBlank(message = "Tên vật tư tiêu hao không được để trống")
    @Size(max = 255, message = "Tên vật tư tiêu hao không được vượt quá 255 ký tự")
    private String name;

    @DecimalMin(value = "0.0", inclusive = true, message = "Đơn giá không được âm")
    @Digits(integer = 8, fraction = 2, message = "Đơn giá không đúng định dạng (tối đa 8 chữ số nguyên và 2 chữ số thập phân)")
    private BigDecimal price;

    @Size(max = 100, message = "Tên nhà sản xuất không được vượt quá 100 ký tự")
    private String manufacturer;

    @NotBlank(message = "Ảnh vật tư tiêu hao không được để trống")
    private String imgPath;

    @NotNull(message = "Đơn vị không được để trống")
    private Integer unitId;

    private String unitName;

    private PartStatus status;
}
