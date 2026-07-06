package com.example.m6_thermal_power_plant_api.service.account;

import com.example.m6_thermal_power_plant_api.dto.accounts.AccountDTO;
import com.example.m6_thermal_power_plant_api.dto.accounts.AccountGrantRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.accounts.AccountStatusUpdateRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.accounts.AccountResponseDTO;
import com.example.m6_thermal_power_plant_api.dto.accounts.WorkerAccountRequest;
import com.example.m6_thermal_power_plant_api.dto.accounts.WorkerAccountResponse;
import com.example.m6_thermal_power_plant_api.entity.Account;

import java.util.List;

public interface IAccountService {
    List<AccountResponseDTO> getAllAccounts();
    AccountResponseDTO createAccount(AccountDTO dto);
    AccountResponseDTO grantAccount(AccountGrantRequestDTO request);
    AccountResponseDTO updateStatus(AccountStatusUpdateRequestDTO request);
    WorkerAccountResponse createWorkerAccount(WorkerAccountRequest req);
}
