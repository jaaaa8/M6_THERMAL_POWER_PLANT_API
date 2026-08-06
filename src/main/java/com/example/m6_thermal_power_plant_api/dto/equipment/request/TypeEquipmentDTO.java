package com.example.m6_thermal_power_plant_api.dto.equipment.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypeEquipmentDTO {

    private Integer equipmentTypeId;
    @NotBlank(message = "Vui lòng điền tên loại thiết bị mới")
    @Size(
            min = 1,
            max = 50,
            message = " Tên loại thiết bị không được vượt quá 50 ký tự."
    )
    private String newEquipmentTypeName;
    @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự.")
    private String description;
}

