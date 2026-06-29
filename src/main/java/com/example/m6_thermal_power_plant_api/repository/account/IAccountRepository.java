package com.example.m6_thermal_power_plant_api.repository.account;

import com.example.m6_thermal_power_plant_api.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAccountRepository extends JpaRepository<Account, Integer> {
    boolean existsByUsername(String username);
    boolean existsByEmployeeId(Integer employeeId);
    boolean existsByEmail(String email);
    java.util.Optional<Account> findByUsername(String username);
}
