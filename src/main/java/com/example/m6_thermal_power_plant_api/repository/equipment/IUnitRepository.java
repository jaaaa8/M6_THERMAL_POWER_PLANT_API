package com.example.m6_thermal_power_plant_api.repository.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.response.UnitListDTO;
import com.example.m6_thermal_power_plant_api.entity.EquipmentParameter;
import com.example.m6_thermal_power_plant_api.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUnitRepository extends JpaRepository<Unit, Integer> {

    boolean existsByNameIgnoreCase(String name);
    List<Unit> findAllByOrderByNameAsc();
}
