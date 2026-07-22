package com.example.m6_thermal_power_plant_api.repository.equipment;

import com.example.m6_thermal_power_plant_api.entity.EquipmentParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IEquipmentParameterRepository extends JpaRepository<EquipmentParameter,Integer> {
    List<EquipmentParameter> findByEquipmentId(Integer equipmentId);
}
