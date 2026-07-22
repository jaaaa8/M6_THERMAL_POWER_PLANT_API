package com.example.m6_thermal_power_plant_api.controller.account;

import com.example.m6_thermal_power_plant_api.dto.accounts.PermissionDTO;
import com.example.m6_thermal_power_plant_api.dto.accounts.RoleDTO;
import com.example.m6_thermal_power_plant_api.dto.accounts.UpdateRolePermissionsRequestDTO;
import com.example.m6_thermal_power_plant_api.service.account.IPermissionService;
import com.example.m6_thermal_power_plant_api.service.account.IRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final IRoleService roleService;
    private final IPermissionService permissionService;

    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @GetMapping("/{id}/permissions")
    public ResponseEntity<List<PermissionDTO>> getRolePermissions(@PathVariable Integer id) {
        return ResponseEntity.ok(permissionService.getPermissionsForRole(id));
    }

    // Đây là API cấu hình ma trận role x permission — thay TOÀN BỘ danh sách
    // permission của role này và bump permission_version cho mọi account đang
    // giữ role đó (cơ chế Cách 2 đã học). Chỉ ADMIN được cấu hình.
    @PutMapping("/{id}/permissions")
    public ResponseEntity<List<PermissionDTO>> updateRolePermissions(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateRolePermissionsRequestDTO request) {
        return ResponseEntity.ok(permissionService.updateRolePermissions(id, request.getPermissionIds()));
    }
}
