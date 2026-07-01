package com.example.m6_thermal_power_plant_api.service.employee;

import com.example.m6_thermal_power_plant_api.dto.employee.EmployeeDTO;
import com.example.m6_thermal_power_plant_api.dto.employee.EmployeeDTO;
import com.example.m6_thermal_power_plant_api.dto.employee.EmployeeResponseDTO;
import com.example.m6_thermal_power_plant_api.entity.Department;
import com.example.m6_thermal_power_plant_api.entity.Employee;
import com.example.m6_thermal_power_plant_api.entity.Expertise;
import com.example.m6_thermal_power_plant_api.entity.Position;
import com.example.m6_thermal_power_plant_api.repository.employee.IEmployeeRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService implements IEmployeeService {

    private final IEmployeeRepository employeeRepository;
    private final EntityManager entityManager;

    private EmployeeResponseDTO mapToResponseDTO(Employee e) {
        if (e == null) return null;
        return EmployeeResponseDTO.builder()
                .id(e.getId())
                .employeeCode(e.getEmployeeCode())
                .fullName(e.getFullName())
                .gmail(e.getGmail())
                .phone(e.getPhone())
                .department(e.getDepartment() != null ? com.example.m6_thermal_power_plant_api.dto.employee.DepartmentDTO.builder()
                        .departmentCode(e.getDepartment().getDepartmentCode())
                        .name(e.getDepartment().getName())
                        .description(e.getDepartment().getDescription())
                        .build() : null)
                .position(e.getPosition() != null ? com.example.m6_thermal_power_plant_api.dto.employee.PositionDTO.builder()
                        .positionCode(e.getPosition().getPositionCode())
                        .name(e.getPosition().getName())
                        .build() : null)
                .expertise(e.getExpertise() != null ? com.example.m6_thermal_power_plant_api.dto.employee.ExpertiseDTO.builder()
                        .expertiseCode(e.getExpertise().getExpertiseCode())
                        .name(e.getExpertise().getName())
                        .build() : null)
                .isActive(e.getIsActive())
                .imgPath(e.getImgPath())
                .build();
    }

    public List<EmployeeResponseDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<com.example.m6_thermal_power_plant_api.dto.employee.EmployeeAccountDTO> getAllEmployeeAccounts() {
        return employeeRepository.findAll().stream()
                .map(e -> {
                    com.example.m6_thermal_power_plant_api.entity.Account a = e.getAccount();
                    return com.example.m6_thermal_power_plant_api.dto.employee.EmployeeAccountDTO.builder()
                            .id(e.getId())
                            .employeeCode(e.getEmployeeCode())
                            .fullName(e.getFullName())
                            .phone(e.getPhone())
                            .department(e.getDepartment() != null ? com.example.m6_thermal_power_plant_api.dto.employee.DepartmentDTO.builder()
                                    .departmentCode(e.getDepartment().getDepartmentCode())
                                    .name(e.getDepartment().getName())
                                    .description(e.getDepartment().getDescription())
                                    .build() : null)
                            .position(e.getPosition() != null ? com.example.m6_thermal_power_plant_api.dto.employee.PositionDTO.builder()
                                    .positionCode(e.getPosition().getPositionCode())
                                    .name(e.getPosition().getName())
                                    .build() : null)
                            .expertise(e.getExpertise() != null ? com.example.m6_thermal_power_plant_api.dto.employee.ExpertiseDTO.builder()
                                    .expertiseCode(e.getExpertise().getExpertiseCode())
                                    .name(e.getExpertise().getName())
                                    .build() : null)
                            .isActive(e.getIsActive())
                            .imgPath(e.getImgPath())
                            .username(a != null ? a.getUsername() : null)
                            .email(e.getGmail())
                            .status(a != null ? a.getStatus() : null)
                            .roles(a != null && a.getRoles() != null ? a.getRoles().stream()
                                    .map(r -> com.example.m6_thermal_power_plant_api.dto.accounts.RoleDTO.builder()
                                            .id(r.getId())
                                            .name(r.getName())
                                            .build())
                                    .collect(java.util.stream.Collectors.toList()) : null)
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public EmployeeResponseDTO createEmployee(EmployeeDTO dto) {
        if (employeeRepository.existsByGmail(dto.getGmail())) {
            throw new IllegalArgumentException("Gmail already exists: " + dto.getGmail());
        }
        if (employeeRepository.existsByPhone(dto.getPhone())) {
            throw new IllegalArgumentException("Phone already exists: " + dto.getPhone());
        }

        Employee employee = new Employee();
        employee.setFullName(dto.getFullName());
        employee.setGmail(dto.getGmail());
        employee.setPhone(dto.getPhone());
        employee.setImgPath(dto.getImgPath());
        
        // Cấp mã nhân viên tự động và đảm bảo không trùng
        String empCode;
        do {
            empCode = "EMP" + System.currentTimeMillis();
        } while (employeeRepository.existsByEmployeeCode(empCode));
        employee.setEmployeeCode(empCode);

        if (dto.getDepartmentId() != null) {
            employee.setDepartment(entityManager.getReference(Department.class, dto.getDepartmentId()));
        }
        if (dto.getExpertiseId() != null) {
            employee.setExpertise(entityManager.getReference(Expertise.class, dto.getExpertiseId()));
        }
        if (dto.getPositionId() != null) {
            employee.setPosition(entityManager.getReference(Position.class, dto.getPositionId()));
        }

        Employee saved = employeeRepository.save(employee);
        employeeRepository.flush();
        return mapToResponseDTO(saved);
    }
}
