package com.example.m6_thermal_power_plant_api.controller.account;

import com.example.m6_thermal_power_plant_api.dto.accounts.AccountDTO;
import com.example.m6_thermal_power_plant_api.dto.accounts.AccountResponseDTO;
import com.example.m6_thermal_power_plant_api.dto.accounts.WorkerAccountRequest;
import com.example.m6_thermal_power_plant_api.dto.accounts.WorkerAccountResponse;
import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.service.account.IAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final IAccountService accountService;

    @GetMapping
    public ResponseEntity<List<AccountResponseDTO>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @PostMapping
    public ResponseEntity<AccountResponseDTO> createAccount(@Valid @RequestBody AccountDTO accountDTO) {
        AccountResponseDTO createdAccount = accountService.createAccount(accountDTO);
        return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
    }

    @PostMapping("/grant")
    public ResponseEntity<AccountResponseDTO> grantAccount(@Valid @RequestBody com.example.m6_thermal_power_plant_api.dto.accounts.AccountGrantRequestDTO request) {
        AccountResponseDTO createdAccount = accountService.grantAccount(request);
        return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
    }

    @PostMapping("/worker")
    public ResponseEntity<WorkerAccountResponse> createWorkerAccount(@Valid @RequestBody WorkerAccountRequest req) {
        return new ResponseEntity<>(accountService.createWorkerAccount(req), HttpStatus.CREATED);
    }

    @PatchMapping("/status")
    public ResponseEntity<AccountResponseDTO> updateStatus(@Valid @RequestBody com.example.m6_thermal_power_plant_api.dto.accounts.AccountStatusUpdateRequestDTO request) {
        AccountResponseDTO updatedAccount = accountService.updateStatus(request);
        return ResponseEntity.ok(updatedAccount);
    }

}