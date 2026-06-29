package com.example.m6_thermal_power_plant_api.service;

import com.example.m6_thermal_power_plant_api.dto.CreateAccountRequestDTO;
import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.Role;
import com.example.m6_thermal_power_plant_api.exception.DuplicateResourceException;
import com.example.m6_thermal_power_plant_api.exception.ResourceNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.AccountRepository;
import com.example.m6_thermal_power_plant_api.repository.RoleRepository;
import com.example.m6_thermal_power_plant_api.service.impl.IAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService implements IAccountService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Account createAccount(CreateAccountRequestDTO request) {
        if (accountRepository.findAccountByUsername(request.getUsername()).isPresent()) {
            throw new DuplicateResourceException("Tên đăng nhập đã tồn tại trong hệ thống!");
        }

        Account newAccount = new Account();
        newAccount.setUsername(request.getUsername());
        newAccount.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        List<Role> roles = request.getRoleNames()
                .stream().map(roleName -> roleRepository.findByName(roleName).orElseThrow(() -> new ResourceNotFoundException("Hệ thống không có role: " + roleName))).toList();

        newAccount.setRoles(roles);

        return accountRepository.save(newAccount);
    }
}
