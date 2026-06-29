package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IEquipmentRepository extends JpaRepository<Equipment, Integer> {
    @Query(value = "SELECT * FROM equipment WHERE id = :equipmentId", nativeQuery = true)
    Optional<Equipment> findByIdIncludingDeleted(@Param("equipmentId") Integer equipmentId);

    /** Lấy thiết bị mới nhất (id lớn nhất), kể cả bản ghi đã bị xoá mềm. */
    @Query(value = "SELECT * FROM equipment ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Optional<Equipment> findLatestIncludingDeleted();
}
