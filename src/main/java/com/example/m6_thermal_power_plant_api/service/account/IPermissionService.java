package com.example.m6_thermal_power_plant_api.service.account;

import com.example.m6_thermal_power_plant_api.dto.accounts.CreatePermissionRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.accounts.PermissionDTO;
import com.example.m6_thermal_power_plant_api.entity.Account;

import java.util.List;

public interface IPermissionService {
    List<PermissionDTO> getAllPermissions();

    PermissionDTO createPermission(CreatePermissionRequestDTO request);

    List<PermissionDTO> getPermissionsForRole(Integer roleId);

    // Thay TOÀN BỘ danh sách permission của 1 role, rồi bump permission_version
    // cho mọi account đang có role đó — đây là bước duy nhất khiến thay đổi
    // "có hiệu lực" (đúng cơ chế Cách 2 đã học).
    List<PermissionDTO> updateRolePermissions(Integer roleId, List<Integer> permissionIds);

    // Gộp permission từ TẤT CẢ role của 1 account thành 1 danh sách code duy nhất
    // (không trùng lặp) — dùng để nhét vào claim "permissions" của access token.
    List<String> resolvePermissionCodes(Account account);
}
