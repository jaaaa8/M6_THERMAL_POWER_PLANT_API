package com.example.m6_thermal_power_plant_api.service.employee;

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
import com.example.m6_thermal_power_plant_api.service.soft_delete.SoftDeleteCascadeService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService implements IEmployeeService {

    private final IEmployeeRepository employeeRepository;
    private final EntityManager entityManager;
    private final SoftDeleteCascadeService softDeleteCascadeService;

    private EmployeeResponseDTO mapToResponseDTO(Employee e) {
        if (e == null) return null;

        com.example.m6_thermal_power_plant_api.dto.employee.DepartmentDTO deptDTO = null;
        try {
            com.example.m6_thermal_power_plant_api.entity.Department d = e.getDepartment();
            if (d != null) {
                d.getName(); // trigger lazy load
                deptDTO = com.example.m6_thermal_power_plant_api.dto.employee.DepartmentDTO.builder()
                        .id(d.getId())
                        .departmentCode(d.getDepartmentCode())
                        .name(d.getName())
                        .description(d.getDescription())
                        .build();
            }
        } catch (jakarta.persistence.EntityNotFoundException ex) {
            // ignore soft-deleted department
        }

        com.example.m6_thermal_power_plant_api.dto.employee.PositionDTO posDTO = null;
        try {
            com.example.m6_thermal_power_plant_api.entity.Position p = e.getPosition();
            if (p != null) {
                p.getName(); // trigger lazy load
                posDTO = com.example.m6_thermal_power_plant_api.dto.employee.PositionDTO.builder()
                        .id(p.getId())
                        .positionCode(p.getPositionCode())
                        .name(p.getName())
                        .build();
            }
        } catch (jakarta.persistence.EntityNotFoundException ex) {
            // ignore soft-deleted position
        }

        com.example.m6_thermal_power_plant_api.dto.employee.ExpertiseDTO expDTO = null;
        try {
            com.example.m6_thermal_power_plant_api.entity.Expertise exp = e.getExpertise();
            if (exp != null) {
                exp.getName(); // trigger lazy load
                expDTO = com.example.m6_thermal_power_plant_api.dto.employee.ExpertiseDTO.builder()
                        .id(exp.getId())
                        .expertiseCode(exp.getExpertiseCode())
                        .name(exp.getName())
                        .build();
            }
        } catch (jakarta.persistence.EntityNotFoundException ex) {
            // ignore soft-deleted expertise
        }

        EmployeeResponseDTO.AccountInfo accountInfo = null;
        try {
            com.example.m6_thermal_power_plant_api.entity.Account a = e.getAccount();
            if (a != null) {
                a.getUsername(); // trigger lazy load
                boolean accountDeleted = Boolean.TRUE.equals(a.getIsDeleted());
                if (!accountDeleted) {
                    java.util.List<com.example.m6_thermal_power_plant_api.dto.accounts.RoleDTO> roleDTOs = null;
                    if (a.getRoles() != null) {
                        roleDTOs = new java.util.ArrayList<>();
                        for (com.example.m6_thermal_power_plant_api.entity.Role r : a.getRoles()) {
                            try {
                                roleDTOs.add(com.example.m6_thermal_power_plant_api.dto.accounts.RoleDTO.builder()
                                        .id(r.getId())
                                        .name(r.getName())
                                        .build());
                            } catch (jakarta.persistence.EntityNotFoundException ex) {
                                // ignore soft-deleted role
                            }
                        }
                    }

                    accountInfo = EmployeeResponseDTO.AccountInfo.builder()
                            .username(a.getUsername())
                            .email(a.getEmail())
                            .status(a.getStatus())
                            .roles(roleDTOs)
                            .build();
                }
            }
        } catch (jakarta.persistence.EntityNotFoundException ex) {
            // ignore soft-deleted account
        }

        return EmployeeResponseDTO.builder()
                .id(e.getId())
                .employeeCode(e.getEmployeeCode())
                .fullName(e.getFullName())
                .gmail(e.getGmail())
                .phone(e.getPhone())
                .department(deptDTO)
                .position(posDTO)
                .expertise(expDTO)
                .isActive(e.getIsActive())
                .imgPath(e.getImgPath())
                .account(accountInfo)
                .build();
    }

    public List<EmployeeResponseDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .filter(e -> !Boolean.TRUE.equals(e.getIsDeleted()))
                .map(this::mapToResponseDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<com.example.m6_thermal_power_plant_api.dto.employee.EmployeeAccountDTO> getAllEmployeeAccounts() {
        return employeeRepository.findAll().stream()
                .filter(e -> !Boolean.TRUE.equals(e.getIsDeleted()))
                .map(e -> {
                    com.example.m6_thermal_power_plant_api.entity.Account a = null;
                    try {
                        a = e.getAccount();
                        if (a != null) {
                            a.getUsername(); // trigger lazy load
                        }
                    } catch (jakarta.persistence.EntityNotFoundException ex) {
                        a = null; // ignore soft-deleted account
                    }
                    com.example.m6_thermal_power_plant_api.entity.Account finalA = a;
                    boolean accountDeleted = finalA != null && Boolean.TRUE.equals(finalA.getIsDeleted());
                    com.example.m6_thermal_power_plant_api.entity.Account resolvedA = accountDeleted ? null : finalA;

                    com.example.m6_thermal_power_plant_api.dto.employee.DepartmentDTO deptDTO = null;
                    try {
                        com.example.m6_thermal_power_plant_api.entity.Department d = e.getDepartment();
                        if (d != null) {
                            d.getName(); // trigger lazy load
                            deptDTO = com.example.m6_thermal_power_plant_api.dto.employee.DepartmentDTO.builder()
                                    .id(d.getId())
                                    .departmentCode(d.getDepartmentCode())
                                    .name(d.getName())
                                    .description(d.getDescription())
                                    .build();
                        }
                    } catch (jakarta.persistence.EntityNotFoundException ex) {
                        // ignore soft-deleted department
                    }

                    com.example.m6_thermal_power_plant_api.dto.employee.PositionDTO posDTO = null;
                    try {
                        com.example.m6_thermal_power_plant_api.entity.Position p = e.getPosition();
                        if (p != null) {
                            p.getName(); // trigger lazy load
                            posDTO = com.example.m6_thermal_power_plant_api.dto.employee.PositionDTO.builder()
                                    .positionCode(p.getPositionCode())
                                    .name(p.getName())
                                    .build();
                        }
                    } catch (jakarta.persistence.EntityNotFoundException ex) {
                        // ignore soft-deleted position
                    }

                    com.example.m6_thermal_power_plant_api.dto.employee.ExpertiseDTO expDTO = null;
                    try {
                        com.example.m6_thermal_power_plant_api.entity.Expertise exp = e.getExpertise();
                        if (exp != null) {
                            exp.getName(); // trigger lazy load
                            expDTO = com.example.m6_thermal_power_plant_api.dto.employee.ExpertiseDTO.builder()
                                    .expertiseCode(exp.getExpertiseCode())
                                    .name(exp.getName())
                                    .build();
                        }
                    } catch (jakarta.persistence.EntityNotFoundException ex) {
                        // ignore soft-deleted expertise
                    }

                    java.util.List<com.example.m6_thermal_power_plant_api.dto.accounts.RoleDTO> roleDTOs = null;
                    if (resolvedA != null && resolvedA.getRoles() != null) {
                        roleDTOs = new java.util.ArrayList<>();
                        for (com.example.m6_thermal_power_plant_api.entity.Role r : resolvedA.getRoles()) {
                            try {
                                roleDTOs.add(com.example.m6_thermal_power_plant_api.dto.accounts.RoleDTO.builder()
                                        .id(r.getId())
                                        .name(r.getName())
                                        .build());
                            } catch (jakarta.persistence.EntityNotFoundException ex) {
                                // ignore soft-deleted role
                            }
                        }
                    }

                    return com.example.m6_thermal_power_plant_api.dto.employee.EmployeeAccountDTO.builder()
                            .id(e.getId())
                            .employeeCode(e.getEmployeeCode())
                            .fullName(e.getFullName())
                            .phone(e.getPhone())
                            .department(deptDTO)
                            .position(posDTO)
                            .expertise(expDTO)
                            .isActive(e.getIsActive())
                            .imgPath(e.getImgPath())
                            .username(resolvedA != null ? resolvedA.getUsername() : null)
                            .email(e.getGmail())
                            .status(resolvedA != null ? resolvedA.getStatus() : null)
                            .roles(roleDTOs)
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
        employee.setImgPath(saveBase64Image(dto.getImgPath()));
        
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

    private String saveBase64Image(String base64Image) {
        if (base64Image == null || base64Image.trim().isEmpty() || !base64Image.startsWith("data:image/")) {
            return base64Image;
        }

        try {
            int commaIndex = base64Image.indexOf(",");
            if (commaIndex == -1) {
                return base64Image;
            }

            String metadata = base64Image.substring(0, commaIndex);
            String base64Data = base64Image.substring(commaIndex + 1);

            String extension = "png";
            int slashIndex = metadata.indexOf("/");
            int semiColonIndex = metadata.indexOf(";");
            if (slashIndex != -1 && semiColonIndex != -1 && semiColonIndex > slashIndex) {
                String mimeType = metadata.substring(slashIndex + 1, semiColonIndex);
                if ("jpeg".equalsIgnoreCase(mimeType) || "jpg".equalsIgnoreCase(mimeType)) {
                    extension = "jpg";
                } else if ("gif".equalsIgnoreCase(mimeType)) {
                    extension = "gif";
                } else if ("webp".equalsIgnoreCase(mimeType)) {
                    extension = "webp";
                } else {
                    extension = mimeType.toLowerCase();
                }
            }

            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            // Path to "images" folder at backend project root
            Path imagesDirPath = Paths.get("images");
            if (!Files.exists(imagesDirPath)) {
                Files.createDirectories(imagesDirPath);
            }

            String filename = "employee_" + UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + "." + extension;
            Path imageFilePath = imagesDirPath.resolve(filename);
            Files.write(imageFilePath, imageBytes);

            return "/api/images/" + filename;
        } catch (Exception e) {
            e.printStackTrace();
            return base64Image; // fallback to original base64 if saving fails
        }
    }

    @Override
    public EmployeeResponseDTO getEmployeeById(Integer id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new com.example.m6_thermal_power_plant_api.exception.ResourceNotFoundException("Employee not found with id: " + id));
        return mapToResponseDTO(employee);
    }

    @Override
    public org.springframework.data.domain.Page<EmployeeResponseDTO> searchEmployees(
            com.example.m6_thermal_power_plant_api.dto.employee.EmployeeSearchRequestDTO searchRequest,
            org.springframework.data.domain.Pageable pageable
    ) {
        org.springframework.data.jpa.domain.Specification<Employee> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            predicates.add(cb.equal(root.get("isDeleted"), false));

            if (searchRequest.getName() != null && !searchRequest.getName().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("fullName")), "%" + searchRequest.getName().trim().toLowerCase() + "%"));
            }
            if (searchRequest.getPhone() != null && !searchRequest.getPhone().trim().isEmpty()) {
                predicates.add(cb.like(root.get("phone"), "%" + searchRequest.getPhone().trim() + "%"));
            }
            if (searchRequest.getGmail() != null && !searchRequest.getGmail().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("gmail")), "%" + searchRequest.getGmail().trim().toLowerCase() + "%"));
            }
            if (searchRequest.getDepartmentId() != null) {
                predicates.add(cb.equal(root.get("department").get("id"), searchRequest.getDepartmentId()));
            }
            if (searchRequest.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), searchRequest.getIsActive()));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return employeeRepository.findAll(spec, pageable).map(this::mapToResponseDTO);
    }

    @Override
    @Transactional
    public EmployeeResponseDTO updateEmployee(Integer id, EmployeeDTO dto) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new com.example.m6_thermal_power_plant_api.exception.ResourceNotFoundException("Employee not found with id: " + id));

        if (employeeRepository.existsByGmailAndIdNot(dto.getGmail(), id)) {
            throw new IllegalArgumentException("Gmail already exists: " + dto.getGmail());
        }
        if (employeeRepository.existsByPhoneAndIdNot(dto.getPhone(), id)) {
            throw new IllegalArgumentException("Phone already exists: " + dto.getPhone());
        }

        existing.setFullName(dto.getFullName());
        existing.setGmail(dto.getGmail());
        existing.setPhone(dto.getPhone());
        
        // Handle image path (decode and save base64 if needed)
        existing.setImgPath(saveBase64Image(dto.getImgPath()));

        if (dto.getDepartmentId() != null) {
            existing.setDepartment(entityManager.getReference(Department.class, dto.getDepartmentId()));
        } else {
            existing.setDepartment(null);
        }

        if (dto.getExpertiseId() != null) {
            existing.setExpertise(entityManager.getReference(Expertise.class, dto.getExpertiseId()));
        } else {
            existing.setExpertise(null);
        }

        if (dto.getPositionId() != null) {
            existing.setPosition(entityManager.getReference(Position.class, dto.getPositionId()));
        } else {
            existing.setPosition(null);
        }

        Employee saved = employeeRepository.save(existing);
        employeeRepository.flush();
        return mapToResponseDTO(saved);
    }

    @Override
    @Transactional
    public void deleteEmployee(Integer id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new com.example.m6_thermal_power_plant_api.exception.ResourceNotFoundException("Employee not found with id: " + id));
        softDeleteCascadeService.softDelete(employee);
    }
}
