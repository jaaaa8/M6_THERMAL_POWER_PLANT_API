package com.example.m6_thermal_power_plant_api.service.department;

import com.example.m6_thermal_power_plant_api.dto.employee.DepartmentDTO;
import java.util.List;

public interface IDepartmentService {
    List<DepartmentDTO> getAllDepartments();
    DepartmentDTO createDepartment(com.example.m6_thermal_power_plant_api.dto.employee.DepartmentCreateDTO dto);
    void deleteDepartment(Integer id);
    org.springframework.data.domain.Page<DepartmentDTO> searchDepartments(com.example.m6_thermal_power_plant_api.dto.employee.DepartmentSearchRequestDTO searchRequest, org.springframework.data.domain.Pageable pageable);
}
