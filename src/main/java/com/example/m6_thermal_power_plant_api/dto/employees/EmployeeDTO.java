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
        return EmployeeDTO.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .fullName(employee.getFullName())
                .gmail(employee.getGmail())
                .phone(employee.getPhone())
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .positionName(employee.getPosition() != null ? employee.getPosition().getName() : null)
                .expertiseName(employee.getExpertise() != null ? employee.getExpertise().getName() : null)
                .isActive(employee.getIsActive())
                .imgPath(employee.getImgPath())
                .build();
    }
}
