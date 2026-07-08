package com.example.m6_thermal_power_plant_api.repository.account;

import com.example.m6_thermal_power_plant_api.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IAccountRepository extends JpaRepository<Account, Integer> {
    boolean existsByUsername(String username);
    boolean existsByEmployeeId(Integer employeeId);
    boolean existsByEmail(String email);
    java.util.Optional<Account> findByUsername(String username);

    @Query("select a.permissionVersion from Account a where a.id = :id")
    java.util.Optional<Integer> findPermissionVersionById(@Param("id") Integer id);

    @Modifying
    @Query("update Account a set a.permissionVersion = a.permissionVersion + 1 " +
           "where a.id in (select acc.id from Account acc join acc.roles r where r.id = :roleId)")
    int bumpPermissionVersionForRole(@Param("roleId") Integer roleId);
}
