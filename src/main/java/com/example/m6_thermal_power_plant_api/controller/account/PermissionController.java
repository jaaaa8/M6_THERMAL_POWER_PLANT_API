package com.example.m6_thermal_power_plant_api.controller.account;

import com.example.m6_thermal_power_plant_api.dto.accounts.CreatePermissionRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.accounts.PermissionDTO;
import com.example.m6_thermal_power_plant_api.service.account.IPermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final IPermissionService permissionService;

    @GetMapping
    public ResponseEntity<List<PermissionDTO>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    // Danh mục permission chỉ ADMIN được tạo mới — tránh tuỳ tiện sinh permission
    // không ai gán, và giữ tên code nhất quán trong toàn hệ thống.
    @PostMapping
    public ResponseEntity<PermissionDTO> createPermission(@Valid @RequestBody CreatePermissionRequestDTO request) {
        return new ResponseEntity<>(permissionService.createPermission(request), HttpStatus.CREATED);
    }
}
