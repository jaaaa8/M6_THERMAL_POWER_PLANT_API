package com.example.m6_thermal_power_plant_api.service.impl;

import com.example.m6_thermal_power_plant_api.dto.CreateAccountRequestDTO;
import com.example.m6_thermal_power_plant_api.entity.Account;

public interface IAccountService {
    Account createAccount(CreateAccountRequestDTO request);
}
