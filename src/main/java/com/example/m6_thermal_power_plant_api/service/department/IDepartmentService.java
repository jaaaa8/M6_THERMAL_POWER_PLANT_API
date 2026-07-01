package com.example.m6_thermal_power_plant_api.service.department;

import com.example.m6_thermal_power_plant_api.dto.employee.DepartmentDTO;
import java.util.List;

public interface IDepartmentService {
    List<DepartmentDTO> getAllDepartments();
}
