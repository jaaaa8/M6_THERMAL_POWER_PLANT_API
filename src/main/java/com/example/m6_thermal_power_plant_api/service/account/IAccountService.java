package com.example.m6_thermal_power_plant_api.service.account;

import com.example.m6_thermal_power_plant_api.dto.accounts.AccountDTO;
import com.example.m6_thermal_power_plant_api.dto.accounts.AccountGrantRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.accounts.AccountStatusUpdateRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.accounts.AccountResponseDTO;
import com.example.m6_thermal_power_plant_api.entity.Account;

import java.util.List;

public interface IAccountService {
    List<AccountResponseDTO> getAllAccounts();
    AccountResponseDTO createAccount(AccountDTO dto);
    AccountResponseDTO grantAccount(AccountGrantRequestDTO request);
    AccountResponseDTO updateStatus(AccountStatusUpdateRequestDTO request);
    org.springframework.data.domain.Page<AccountResponseDTO> searchAccounts(com.example.m6_thermal_power_plant_api.dto.accounts.AccountSearchRequestDTO searchRequest, org.springframework.data.domain.Pageable pageable);
    AccountResponseDTO getAccountById(Integer id);
    AccountResponseDTO updateAccount(Integer id, AccountDTO dto);
    void resetPassword(Integer id);
}
