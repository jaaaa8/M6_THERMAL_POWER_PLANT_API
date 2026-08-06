package com.example.m6_thermal_power_plant_api.repository.equipment;

import com.example.m6_thermal_power_plant_api.entity.EquipmentSystem;
import com.example.m6_thermal_power_plant_api.entity.EquipmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IEquipmentTypeRepository extends JpaRepository<EquipmentType, Integer> {
    Optional<EquipmentType> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
