package com.example.m6_thermal_power_plant_api.controller.account;

import com.example.m6_thermal_power_plant_api.dto.CreateAccountRequestDTO;
import com.example.m6_thermal_power_plant_api.exception.ApiResponse;
import com.example.m6_thermal_power_plant_api.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> createAccount(@Valid @RequestBody CreateAccountRequestDTO request) {
        accountService.createAccount(request);
        return ApiResponse.created("Tạo tài khoản thành công");
    }
}
