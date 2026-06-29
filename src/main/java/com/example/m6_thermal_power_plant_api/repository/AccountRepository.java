package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    // Dùng cho login flow (verify password) — chỉ cần roles
    @EntityGraph(attributePaths = "roles")
    Optional<Account> findAccountByUsername(String username);

    // Dùng cho /me và build UserInfoDTO — eager fetch employee + department
    // để tránh LazyInitializationException ngoài transaction.
    @EntityGraph(attributePaths = {"roles", "employee", "employee.department"})
    Optional<Account> findWithEmployeeById(Integer id);
}
