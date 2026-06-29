package com.example.m6_thermal_power_plant_api.dto.accounts;

import jakarta.validation.constraints.Digits;
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
public class AccountDTO {
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 8, max = 50, message = "Username must be between 8 and 50 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[0-9])[a-z0-9]+$", message = "Username must contain only lowercase letters and numbers, and include at least one of each")
    private String username;

    @jakarta.validation.constraints.Email(message = "Email should be valid")
    private String email;


    @Digits(integer = 10, fraction = 0, message = "Employee ID must be a number")
    private Integer employeeId;

    @NotNull(message = "Roles cannot be empty")
    @Size(min = 1, message = "At least one role must be selected")
    @com.fasterxml.jackson.annotation.JsonFormat(with = com.fasterxml.jackson.annotation.JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private java.util.List<Integer> roleIds;
}
