package com.example.m6_thermal_power_plant_api.dto.employees;

import com.example.m6_thermal_power_plant_api.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDTO {
    private Integer id;
    private String employeeCode;
    private String fullName;
    private String gmail;
    private String phone;
    private String departmentName;
    private String positionName;
    private String expertiseName;
    private Boolean isActive;
    private String imgPath;

    public static EmployeeDTO from(Employee employee) {
        String deptName = null;
        try {
            if (employee.getDepartment() != null) {
                deptName = employee.getDepartment().getName();
            }
        } catch (jakarta.persistence.EntityNotFoundException e) {
            // ignore soft-deleted department
        }

        String posName = null;
        try {
            if (employee.getPosition() != null) {
                posName = employee.getPosition().getName();
            }
        } catch (jakarta.persistence.EntityNotFoundException e) {
            // ignore soft-deleted position
        }

        String expName = null;
        try {
            if (employee.getExpertise() != null) {
                expName = employee.getExpertise().getName();
            }
        } catch (jakarta.persistence.EntityNotFoundException e) {
            // ignore soft-deleted expertise
        }

        return EmployeeDTO.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .fullName(employee.getFullName())
                .gmail(employee.getGmail())
                .phone(employee.getPhone())
                .departmentName(deptName)
                .positionName(posName)
                .expertiseName(expName)
                .isActive(employee.getIsActive())
                .imgPath(employee.getImgPath())
                .build();
    }
}
