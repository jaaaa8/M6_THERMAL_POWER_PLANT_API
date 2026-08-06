package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.WorkOrderEquipment;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderEquipmentStatus;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkOrderEquipmentRepository extends JpaRepository<WorkOrderEquipment, Integer> {

    Optional<WorkOrderEquipment> findByWorkOrder_IdAndEquipment_Id(Integer workOrderId, Integer equipmentId);

    List<WorkOrderEquipment> findByWorkOrder_Id(Integer workOrderId);

    @Query("""
        SELECT COUNT(woe) > 0 FROM WorkOrderEquipment woe
        JOIN woe.workOrder wo
        WHERE woe.lubricationPlan.id = :planId
          AND wo.status IN :liveStatuses
    """)
    boolean existsLiveLubricationWoeByPlanId(
            @Param("planId") Integer planId,
            @Param("liveStatuses") java.util.Collection<WorkOrderStatus> liveStatuses);
}
