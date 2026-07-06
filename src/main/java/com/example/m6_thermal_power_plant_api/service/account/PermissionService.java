package com.example.m6_thermal_power_plant_api.service.account;

import com.example.m6_thermal_power_plant_api.dto.accounts.CreatePermissionRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.accounts.PermissionDTO;
import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.Permission;
import com.example.m6_thermal_power_plant_api.entity.Role;
import com.example.m6_thermal_power_plant_api.exception.ResourceNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.account.IAccountRepository;
import com.example.m6_thermal_power_plant_api.repository.account.IPermissionRepository;
import com.example.m6_thermal_power_plant_api.repository.account.IRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService implements IPermissionService {

    private final IPermissionRepository permissionRepository;
    private final IRoleRepository roleRepository;
    private final IAccountRepository accountRepository;

    @Override
    public List<PermissionDTO> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PermissionDTO createPermission(CreatePermissionRequestDTO request) {
        String code = request.getCode().trim().toUpperCase();
        if (permissionRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Permission code đã tồn tại: " + code);
        }
        Permission permission = Permission.builder()
                .code(code)
                .description(request.getDescription())
                .build();
        return toDto(permissionRepository.save(permission));
    }

    @Override
    public List<PermissionDTO> getPermissionsForRole(Integer roleId) {
        Role role = roleRepository.findWithPermissionsById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role không tồn tại: " + roleId));
        return role.getPermissions() == null ? Collections.emptyList()
                : role.getPermissions().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<PermissionDTO> updateRolePermissions(Integer roleId, List<Integer> permissionIds) {
        Role role = roleRepository.findWithPermissionsById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role không tồn tại: " + roleId));

        List<Permission> permissions = permissionIds.isEmpty()
                ? Collections.emptyList()
                : permissionRepository.findByIdIn(permissionIds);

        if (permissions.size() != permissionIds.size()) {
            throw new IllegalArgumentException("Có permissionId không tồn tại trong danh sách gửi lên");
        }

        role.setPermissions(new HashSet<>(permissions));
        roleRepository.save(role);

        // Điểm mấu chốt của Cách 2: đánh dấu "cũ" toàn bộ account đang giữ role này,
        // để access token hiện có của họ bị từ chối ở request/refresh tiếp theo.
        accountRepository.bumpPermissionVersionForRole(roleId);

        return permissions.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<String> resolvePermissionCodes(Account account) {
        if (account.getRoles() == null) return Collections.emptyList();
        return account.getRoles().stream()
                .filter(r -> r.getPermissions() != null)
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getCode)
                .distinct()
                .collect(Collectors.toList());
    }

    private PermissionDTO toDto(Permission p) {
        return PermissionDTO.builder()
                .id(p.getId())
                .code(p.getCode())
                .description(p.getDescription())
                .build();
    }
}
