package com.example.m6_thermal_power_plant_api.service.impl;

import com.example.m6_thermal_power_plant_api.dto.LoginRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.LoginResponseDTO;
import com.example.m6_thermal_power_plant_api.dto.RefreshTokenRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.RefreshTokenResponseDTO;
import com.example.m6_thermal_power_plant_api.dto.UserInfoDTO;
import com.example.m6_thermal_power_plant_api.dto.ChangePasswordRequestDTO;

public interface IAuthService {
    LoginResponseDTO login(LoginRequestDTO request);

    RefreshTokenResponseDTO refreshToken(RefreshTokenRequestDTO request);

    void logout(String username);

    UserInfoDTO getMe(Integer accountId);

    void changePassword(Integer accountId, ChangePasswordRequestDTO request);
}
