package com.example.m6_thermal_power_plant_api.controller.employee;

import com.example.m6_thermal_power_plant_api.dto.employee.DepartmentDTO;
import com.example.m6_thermal_power_plant_api.dto.employee.DepartmentCreateDTO;
import com.example.m6_thermal_power_plant_api.dto.employee.DepartmentUpdateDTO;
import com.example.m6_thermal_power_plant_api.service.department.IDepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final IDepartmentService departmentService;

    @GetMapping
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @PostMapping
    public ResponseEntity<DepartmentDTO> createDepartment(@Valid @RequestBody DepartmentCreateDTO dto) {
        DepartmentDTO created = departmentService.createDepartment(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Integer id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<org.springframework.data.domain.Page<DepartmentDTO>> searchDepartments(
            @ModelAttribute com.example.m6_thermal_power_plant_api.dto.employee.DepartmentSearchRequestDTO searchRequest,
            org.springframework.data.domain.Pageable pageable
    ) {
        return ResponseEntity.ok(departmentService.searchDepartments(searchRequest, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentDTO> updateDepartment(
            @PathVariable Integer id,
            @Valid @RequestBody DepartmentUpdateDTO dto
    ) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDTO> getDepartmentById(@PathVariable Integer id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }
}
