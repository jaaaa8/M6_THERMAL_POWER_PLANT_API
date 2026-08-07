package com.example.m6_thermal_power_plant_api.controller.account;

import com.example.m6_thermal_power_plant_api.dto.accounts.AccountDTO;
import com.example.m6_thermal_power_plant_api.dto.accounts.AccountResponseDTO;

import com.example.m6_thermal_power_plant_api.service.account.IAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasAnyRole('HR_STAFF','TOOLS_STOREKEEPER')")
    @PostMapping
    public ResponseEntity<AccountResponseDTO> createAccount(@Valid @RequestBody AccountDTO accountDTO) {
        AccountResponseDTO createdAccount = accountService.createAccount(accountDTO);
        return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('HR_STAFF','TOOLS_STOREKEEPER')")
    @PostMapping("/grant")
    public ResponseEntity<AccountResponseDTO> grantAccount(@Valid @RequestBody com.example.m6_thermal_power_plant_api.dto.accounts.AccountGrantRequestDTO request) {
        AccountResponseDTO createdAccount = accountService.grantAccount(request);
        return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('HR_STAFF')")
    @PatchMapping("/status")
    public ResponseEntity<AccountResponseDTO> updateStatus(@Valid @RequestBody com.example.m6_thermal_power_plant_api.dto.accounts.AccountStatusUpdateRequestDTO request) {
        AccountResponseDTO updatedAccount = accountService.updateStatus(request);
        return ResponseEntity.ok(updatedAccount);
    }

    @GetMapping("/search")
    public ResponseEntity<org.springframework.data.domain.Page<AccountResponseDTO>> searchAccounts(
            @ModelAttribute com.example.m6_thermal_power_plant_api.dto.accounts.AccountSearchRequestDTO searchRequest,
            org.springframework.data.domain.Pageable pageable
    ) {
        return ResponseEntity.ok(accountService.searchAccounts(searchRequest, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseDTO> getAccountById(@PathVariable Integer id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @PreAuthorize("hasRole('HR_STAFF')")
    @PutMapping("/{id}")
    public ResponseEntity<AccountResponseDTO> updateAccount(
            @PathVariable Integer id,
            @Valid @RequestBody AccountDTO accountDTO
    ) {
        AccountResponseDTO updated = accountService.updateAccount(id, accountDTO);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('HR_STAFF')")
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Void> resetPassword(@PathVariable Integer id) {
        accountService.resetPassword(id);
        return ResponseEntity.ok().build();
    }

}