package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    // Dùng cho login flow (verify password) — cần roles.permissions để build
    // claim "permissions" của access token ngay lúc login.
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<Account> findAccountByUsername(String username);

    // Dùng cho /me và build UserInfoDTO — eager fetch employee + department
    // để tránh LazyInitializationException ngoài transaction.
    @EntityGraph(attributePaths = {"roles", "roles.permissions", "employee", "employee.department"})
    Optional<Account> findWithEmployeeById(Integer id);

    // Tra cứu RẺ (chỉ 1 cột int) — dùng ở jwtAuthFilter để so sánh mỗi
    // request với claim "permVer" trong token (cơ chế Cách 2).
    @Query("select a.permissionVersion from Account a where a.id = :id")
    Optional<Integer> findPermissionVersionById(@Param("id") Integer id);
}
