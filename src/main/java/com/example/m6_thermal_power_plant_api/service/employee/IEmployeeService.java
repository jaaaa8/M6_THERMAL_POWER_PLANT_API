package com.example.m6_thermal_power_plant_api.service.employee;

import com.example.m6_thermal_power_plant_api.dto.employee.EmployeeDTO;
import com.example.m6_thermal_power_plant_api.dto.employee.EmployeeDTO;
import com.example.m6_thermal_power_plant_api.dto.employee.EmployeeResponseDTO;
import com.example.m6_thermal_power_plant_api.dto.employee.EmployeeAccountDTO;
import com.example.m6_thermal_power_plant_api.entity.Employee;

import java.util.List;

public interface IEmployeeService {
    List<EmployeeResponseDTO> getAllEmployees();
    List<EmployeeAccountDTO> getAllEmployeeAccounts();
    EmployeeResponseDTO createEmployee(EmployeeDTO dto);
    EmployeeResponseDTO getEmployeeById(Integer id);
    EmployeeResponseDTO updateEmployee(Integer id, EmployeeDTO dto);
    org.springframework.data.domain.Page<EmployeeResponseDTO> searchEmployees(
            com.example.m6_thermal_power_plant_api.dto.employee.EmployeeSearchRequestDTO searchRequest,
            org.springframework.data.domain.Pageable pageable
    );
}
