package com.example.m6_thermal_power_plant_api.service.department;

import com.example.m6_thermal_power_plant_api.dto.employee.DepartmentDTO;
import com.example.m6_thermal_power_plant_api.dto.employee.DepartmentCreateDTO;
import com.example.m6_thermal_power_plant_api.dto.employee.DepartmentUpdateDTO;
import com.example.m6_thermal_power_plant_api.entity.Department;
import com.example.m6_thermal_power_plant_api.exception.DuplicateResourceException;
import com.example.m6_thermal_power_plant_api.repository.department.IDepartmentRepository;
import com.example.m6_thermal_power_plant_api.service.soft_delete.SoftDeleteCascadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService implements IDepartmentService {

    private final IDepartmentRepository departmentRepository;
    private final SoftDeleteCascadeService softDeleteCascadeService;

    @Override
    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .filter(d -> !Boolean.TRUE.equals(d.getIsDeleted()))
                .map(d -> DepartmentDTO.builder()
                        .id(d.getId())
                        .departmentCode(d.getDepartmentCode())
                        .name(d.getName())
                        .description(d.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DepartmentDTO createDepartment(DepartmentCreateDTO dto) {
        String deptCode = dto.getDepartmentCode().trim().toUpperCase();
        if (departmentRepository.existsByDepartmentCode(deptCode)) {
            throw new DuplicateResourceException("Mã phòng ban '" + deptCode + "' đã tồn tại.");
        }

        String name = dto.getName().trim();
        if (departmentRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Tên phòng ban '" + name + "' đã tồn tại.");
        }

        Department dept = Department.builder()
                .departmentCode(deptCode)
                .name(name)
                .description(dto.getDescription())
                .build();
        Department saved = departmentRepository.save(dept);
        return DepartmentDTO.builder()
                .id(saved.getId())
                .departmentCode(saved.getDepartmentCode())
                .name(saved.getName())
                .description(saved.getDescription())
                .build();
    }

    @Override
    @Transactional
    public void deleteDepartment(Integer id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found for id: " + id));
        softDeleteCascadeService.softDelete(dept);
    }

    @Override
    public org.springframework.data.domain.Page<DepartmentDTO> searchDepartments(
            com.example.m6_thermal_power_plant_api.dto.employee.DepartmentSearchRequestDTO searchRequest,
            org.springframework.data.domain.Pageable pageable
    ) {
        org.springframework.data.jpa.domain.Specification<Department> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            predicates.add(cb.equal(root.get("isDeleted"), false));

            if (searchRequest.getDepartmentCode() != null && !searchRequest.getDepartmentCode().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("departmentCode")), "%" + searchRequest.getDepartmentCode().trim().toLowerCase() + "%"));
            }

            if (searchRequest.getName() != null && !searchRequest.getName().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + searchRequest.getName().trim().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return departmentRepository.findAll(spec, pageable).map(d -> DepartmentDTO.builder()
                .id(d.getId())
                .departmentCode(d.getDepartmentCode())
                .name(d.getName())
                .description(d.getDescription())
                .build());
    }

    @Override
    @Transactional
    public DepartmentDTO updateDepartment(Integer id, DepartmentUpdateDTO dto) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found for id: " + id));

        String name = dto.getName().trim();
        if (departmentRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateResourceException("Tên phòng ban '" + name + "' đã tồn tại.");
        }

        dept.setName(name);
        dept.setDescription(dto.getDescription());
        Department saved = departmentRepository.save(dept);
        return DepartmentDTO.builder()
                .id(saved.getId())
                .departmentCode(saved.getDepartmentCode())
                .name(saved.getName())
                .description(saved.getDescription())
                .build();
    }

    @Override
    public DepartmentDTO getDepartmentById(Integer id) {
        Department d = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found for id: " + id));
        return DepartmentDTO.builder()
                .id(d.getId())
                .departmentCode(d.getDepartmentCode())
                .name(d.getName())
                .description(d.getDescription())
                .build();
    }
}
