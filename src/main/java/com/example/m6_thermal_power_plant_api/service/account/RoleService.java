package com.example.m6_thermal_power_plant_api.service.account;

import com.example.m6_thermal_power_plant_api.dto.accounts.RoleDTO;
import com.example.m6_thermal_power_plant_api.repository.account.IRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService implements IRoleService {

    private final IRoleRepository roleRepository;

    @Override
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(r -> RoleDTO.builder()
                        .id(r.getId())
                        .name(r.getName())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }
}
