package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.ConsumableIssue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IConsumableIssueRepository extends JpaRepository<ConsumableIssue, Integer> {

    /** Các phiếu cấp vật tư tiêu hao của một phiếu công tác, mới nhất trước. */
    List<ConsumableIssue> findByWorkOrder_IdOrderByIssuedAtDesc(Integer workOrderId);
}
