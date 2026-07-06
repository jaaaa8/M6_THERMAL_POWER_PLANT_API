package com.example.m6_thermal_power_plant_api.repository.account;

import com.example.m6_thermal_power_plant_api.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IAccountRepository extends JpaRepository<Account, Integer> {
    boolean existsByUsername(String username);
    boolean existsByEmployeeId(Integer employeeId);
    boolean existsByEmail(String email);
    Optional<Account> findByUsername(String username);

    @Query("SELECT DISTINCT a FROM Account a JOIN a.roles r WHERE r.name IN :roleNames")
    List<Account> findByRoleNames(@Param("roleNames") List<String> roleNames);
}
