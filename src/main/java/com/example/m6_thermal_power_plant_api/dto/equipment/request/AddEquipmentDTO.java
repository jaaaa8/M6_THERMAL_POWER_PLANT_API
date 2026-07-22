package com.example.m6_thermal_power_plant_api.dto.equipment.request;

import com.example.m6_thermal_power_plant_api.entity.enums.EquipmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddEquipmentDTO {

    @NotBlank(message = "Tên thiết bị không được để trống")
    @Size(min = 10, max = 255, message = "Tên thiết bị phải từ 10 đến 255 ký tự")
    @Pattern(
            regexp = "^[A-ZÀ-Ỹ][a-zA-ZÀ-ỹ0-9\\s\\-_,()./]*$",
            message = "Tên thiết bị phải bắt đầu bằng chữ hoa và chỉ chứa chữ, số cùng các ký tự hợp lệ"
    )
    private String name;

    @NotNull (message = "Loại thiết bị không được để trống !")
    private Integer equipmentTypeId;

    @NotNull (message = "Trạng thái không được để trống !")
    private EquipmentStatus status;

    private Integer installationYear;

    private String manufacturer;

    private String model;

    private String description;
}
