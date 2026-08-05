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
import com.example.m6_thermal_power_plant_api.dto.file.FileUploadResult;
import com.example.m6_thermal_power_plant_api.service.util.FileUploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final IEmployeeService employeeService;
    private final IDepartmentService departmentService;
    private final IPositionService positionService;
    private final IExpertiseService expertiseService;
    private final FileUploadService fileUploadService;

    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<com.example.m6_thermal_power_plant_api.dto.employee.EmployeeAccountDTO>> getAllEmployeeAccounts() {
        return ResponseEntity.ok(employeeService.getAllEmployeeAccounts());
    }

    @PreAuthorize("hasAnyRole('HR_STAFF')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EmployeeResponseDTO> createEmployee(
            @RequestPart("employee") @Valid EmployeeDTO employeeDTO,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        if (image != null && !image.isEmpty()) {
            FileUploadResult uploadResult = fileUploadService.uploadImage(image);
            employeeDTO.setImgPath(uploadResult.secureUrl());
        }
        EmployeeResponseDTO createdEmployee = employeeService.createEmployee(employeeDTO);
        return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable Integer id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
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

    @PreAuthorize("hasAnyRole('HR_STAFF')")
    @PostMapping("/positions")
    public ResponseEntity<PositionDTO> createPosition(@Valid @RequestBody PositionDTO positionDTO) {
        PositionDTO created = positionService.createPosition(positionDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('HR_STAFF')")
    @PostMapping("/expertises")
    public ResponseEntity<ExpertiseDTO> createExpertise(@Valid @RequestBody ExpertiseDTO expertiseDTO) {
        ExpertiseDTO created = expertiseService.createExpertise(expertiseDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/search")
    public ResponseEntity<org.springframework.data.domain.Page<EmployeeResponseDTO>> searchEmployees(
            @ModelAttribute com.example.m6_thermal_power_plant_api.dto.employee.EmployeeSearchRequestDTO searchRequest,
            org.springframework.data.domain.Pageable pageable
    ) {
        return ResponseEntity.ok(employeeService.searchEmployees(searchRequest, pageable));
    }

    @PreAuthorize("hasAnyRole('HR_STAFF')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(
            @PathVariable Integer id,
            @RequestPart("employee") @Valid EmployeeDTO employeeDTO,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        if (image != null && !image.isEmpty()) {
            FileUploadResult uploadResult = fileUploadService.uploadImage(image);
            employeeDTO.setImgPath(uploadResult.secureUrl());
        }
        EmployeeResponseDTO updated = employeeService.updateEmployee(id, employeeDTO);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasAnyRole('HR_STAFF')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Integer id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
