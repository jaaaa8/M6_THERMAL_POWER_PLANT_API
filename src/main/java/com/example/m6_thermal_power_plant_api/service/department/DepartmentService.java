package com.example.m6_thermal_power_plant_api.service.department;

import com.example.m6_thermal_power_plant_api.dto.employee.DepartmentDTO;
import com.example.m6_thermal_power_plant_api.repository.department.IDepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService implements IDepartmentService {

    private final IDepartmentRepository departmentRepository;

    @Override
    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(d -> DepartmentDTO.builder()
                        .departmentCode(d.getDepartmentCode())
                        .name(d.getName())
                        .description(d.getDescription())
                        .build())
                .collect(Collectors.toList());
    }
}
