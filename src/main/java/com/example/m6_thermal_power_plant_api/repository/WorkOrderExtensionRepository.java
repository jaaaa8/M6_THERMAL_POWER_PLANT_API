package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.WorkOrderExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkOrderExtensionRepository extends JpaRepository<WorkOrderExtension, Integer> {

    /** Lịch sử gia hạn của một phiếu công tác, theo thứ tự ngày gia hạn tăng dần. */
    List<WorkOrderExtension> findByWorkOrder_IdOrderByExtendedUntilAsc(Integer workOrderId);
}
