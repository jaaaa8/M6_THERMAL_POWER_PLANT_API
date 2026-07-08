package com.example.m6_thermal_power_plant_api.dto.accounts;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateRolePermissionsRequestDTO {
    @NotNull(message = "permissionIds không được null (để rỗng nếu muốn gỡ hết quyền)")
    private List<Integer> permissionIds;
}
