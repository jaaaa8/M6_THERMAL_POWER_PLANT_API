package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.WorkOrderEquipment;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderEquipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkOrderEquipmentRepository extends JpaRepository<WorkOrderEquipment, Integer> {

    Optional<WorkOrderEquipment> findByWorkOrder_IdAndEquipment_Id(Integer workOrderId, Integer equipmentId);

    List<WorkOrderEquipment> findByWorkOrder_Id(Integer workOrderId);
}
