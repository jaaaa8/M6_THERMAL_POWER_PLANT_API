package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.SparePartsIssue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ISparePartsIssueRepository extends JpaRepository<SparePartsIssue, Integer> {

    /** Các phiếu cấp vật tư thay thế của một phiếu công tác, mới nhất trước. */
    List<SparePartsIssue> findByWorkOrder_IdOrderByIssuedAtDesc(Integer workOrderId);
}
