package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.RepairHistory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IRepairHistoryRepository extends JpaRepository<RepairHistory, Integer> {
    boolean existsByWorkOrderId(Integer id);
    List<RepairHistory> findByEquipmentIdOrderByRepairDateDesc(Integer equipmentId);
}
