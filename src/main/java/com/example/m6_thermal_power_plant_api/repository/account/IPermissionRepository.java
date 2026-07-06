package com.example.m6_thermal_power_plant_api.repository.account;

import com.example.m6_thermal_power_plant_api.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPermissionRepository extends JpaRepository<Permission, Integer> {
    boolean existsByCode(String code);
    List<Permission> findByIdIn(List<Integer> ids);
}
