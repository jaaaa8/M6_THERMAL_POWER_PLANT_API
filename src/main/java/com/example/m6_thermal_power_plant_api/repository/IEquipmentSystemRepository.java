package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.EquipmentSystem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IEquipmentSystemRepository extends JpaRepository<EquipmentSystem, Integer> {
    Page<EquipmentSystem> findByNameContainingIgnoreCase(
            String word, Pageable pageable
    );
    boolean existsByNameIgnoreCase(String name);


}
