package com.example.m6_thermal_power_plant_api.controller.employee;

import com.example.m6_thermal_power_plant_api.dto.employee.DepartmentDTO;
import com.example.m6_thermal_power_plant_api.dto.employee.EmployeeDTO;
import com.example.m6_thermal_power_plant_api.dto.employee.EmployeeDTO;
import com.example.m6_thermal_power_plant_api.dto.employee.EmployeeResponseDTO;
import com.example.m6_thermal_power_plant_api.dto.employee.ExpertiseDTO;
import com.example.m6_thermal_power_plant_api.dto.employee.PositionDTO;
import com.example.m6_thermal_power_plant_api.entity.Employee;
import com.example.m6_thermal_power_plant_api.service.department.IDepartmentService;
import com.example.m6_thermal_power_plant_api.service.employee.IEmployeeService;
import com.example.m6_thermal_power_plant_api.service.expertise.IExpertiseService;
import com.example.m6_thermal_power_plant_api.service.position.IPositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final IEmployeeService employeeService;
    private final IDepartmentService departmentService;
    private final IPositionService positionService;
    private final IExpertiseService expertiseService;

    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<com.example.m6_thermal_power_plant_api.dto.employee.EmployeeAccountDTO>> getAllEmployeeAccounts() {
        return ResponseEntity.ok(employeeService.getAllEmployeeAccounts());
    }

    @PostMapping
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody EmployeeDTO employeeDTO) {
        Employee createdEmployee = employeeService.createEmployee(employeeDTO);
        return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
    }

    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @GetMapping("/positions")
    public ResponseEntity<List<PositionDTO>> getAllPositions() {
        return ResponseEntity.ok(positionService.getAllPositions());
    }

    @GetMapping("/expertises")
    public ResponseEntity<List<ExpertiseDTO>> getAllExpertises() {
        return ResponseEntity.ok(expertiseService.getAllExpertises());
    }
}
