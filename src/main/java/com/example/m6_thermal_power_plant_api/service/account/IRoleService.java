package com.example.m6_thermal_power_plant_api.service.account;

import com.example.m6_thermal_power_plant_api.dto.accounts.RoleDTO;

import java.util.List;

public interface IRoleService {
    List<RoleDTO> getAllRoles();
}
