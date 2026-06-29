package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.WorkOrderMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkOrderMemberRepository extends JpaRepository<WorkOrderMember, Integer> {

    /** Danh sách thành viên (đang active) của một phiếu công tác. */
    List<WorkOrderMember> findByWorkOrder_Id(Integer workOrderId);
}
