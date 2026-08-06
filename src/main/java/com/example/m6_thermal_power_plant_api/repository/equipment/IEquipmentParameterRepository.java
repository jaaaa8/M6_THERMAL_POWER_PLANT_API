package com.example.m6_thermal_power_plant_api.repository.equipment;

import com.example.m6_thermal_power_plant_api.entity.EquipmentParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IEquipmentParameterRepository extends JpaRepository<EquipmentParameter,Integer> {
    List<EquipmentParameter> findByEquipmentId(Integer equipmentId);
    @Query("""
    SELECT COUNT(ep) > 0
    FROM EquipmentParameter ep
    WHERE ep.equipment.id = :equipmentId
      AND ep.parameter.id = :parameterId
      AND ep.unit.id = :unitId
      AND ep.isDeleted = false
""")
    boolean exists(
            Integer equipmentId,
            Integer parameterId,
            Integer unitId
    );

    boolean existsByEquipmentIdAndParameterIdAndUnitIdAndIdNotAndIsDeletedFalse(
            Integer equipmentId,
            Integer parameterId,
            Integer unitId,
            Integer id
    );
}
