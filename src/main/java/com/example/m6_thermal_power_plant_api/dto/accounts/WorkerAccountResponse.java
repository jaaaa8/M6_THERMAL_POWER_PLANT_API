package com.example.m6_thermal_power_plant_api.dto.accounts;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkerAccountResponse {
    private Integer accountId;
    private String username;
    private String password;
    private String fullName;
    private String email;
}
