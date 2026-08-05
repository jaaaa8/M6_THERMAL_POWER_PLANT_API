package com.example.m6_thermal_power_plant_api.dto.equipment.request;

import com.example.m6_thermal_power_plant_api.dto.equipment.response.UnitListDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParameterDTO {
    private Integer id;

    private Integer equipmentId;

    private Integer parameterId;
    @NotBlank(message = "Vui lòng điền tên thông số")
    @Size(
            min = 1,
            max = 50,
            message = "Tên đơn vị không được vượt quá 50 ký tự."
    )
    @Pattern(
            regexp = "^[A-Za-z0-9°%μµΩΩÀ-ỹ³²^/().·*_-]+$",
            message = "Tên đơn vị chứa ký tự không hợp lệ."
    )
    private String name;
    @NotBlank(message = "Vui lòng điền giá trị của thông số")
    @Size(
            min = 1,
            message = "Giá trị không hợp lệ."
    )
    private String value;
    private Integer unitId;
    private String unitName;
    private String description;
}
