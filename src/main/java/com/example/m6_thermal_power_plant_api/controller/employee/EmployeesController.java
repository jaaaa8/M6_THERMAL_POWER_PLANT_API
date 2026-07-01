package com.example.m6_thermal_power_plant_api.controller.employee;

import com.example.m6_thermal_power_plant_api.dto.employees.EmployeeDTO;
import com.example.m6_thermal_power_plant_api.entity.Employee;
import com.example.m6_thermal_power_plant_api.exception.ApiResponse;
import com.example.m6_thermal_power_plant_api.repository.EmployeeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
public class EmployeesController {
    private final EmployeeRepository employeeRepository;

    public EmployeesController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeDTO>>> findAll() {
        List<Employee> employees = employeeRepository.findAll();
        List<EmployeeDTO> dtos = employees.stream()
                .map(EmployeeDTO::from)
                .collect(Collectors.toList());
        return ApiResponse.success(dtos);
    }
}
