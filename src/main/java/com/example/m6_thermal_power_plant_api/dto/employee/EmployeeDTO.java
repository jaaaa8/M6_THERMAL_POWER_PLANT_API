package com.example.m6_thermal_power_plant_api.dto.employee;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Data
public class EmployeeDTO {
    @NotBlank(message = "Full name cannot be blank")
    @Size(min = 1, max = 50, message = "Full name must be between 1 and 50 characters")
    private String fullName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String gmail;

    private String imgPath;

    @NotBlank(message = "Phone cannot be blank")
    @Pattern(regexp = "^\\d{10,11}$", message = "Invalid phone number format")
    private String phone;

    @NotNull(message = "Department ID cannot be null")
    @Digits(integer = 10, fraction = 0, message = "Department ID must be a number")
    private Integer departmentId;

    @NotNull(message = "Expertise ID cannot be null")
    @Digits(integer = 10, fraction = 0, message = "Expertise ID must be a number")
    private Integer expertiseId;

    @NotNull(message = "Position ID cannot be null")
    @Digits(integer = 10, fraction = 0, message = "Position ID must be a number")
    private Integer positionId;
}
