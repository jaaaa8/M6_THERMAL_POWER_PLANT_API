package com.example.m6_thermal_power_plant_api.dto.accounts;

import com.example.m6_thermal_power_plant_api.entity.enums.AccountStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatusUpdateRequestDTO {

    @NotBlank(message = "Username cannot be blank")
    private String username;

    @NotNull(message = "Status cannot be null")
    private AccountStatus status;


}
