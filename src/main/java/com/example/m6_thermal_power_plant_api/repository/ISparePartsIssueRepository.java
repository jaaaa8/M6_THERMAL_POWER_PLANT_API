package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.SparePartsIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ISparePartsIssueRepository extends JpaRepository<SparePartsIssue, Integer> {

    /** Các phiếu cấp vật tư thay thế của một phiếu công tác, mới nhất trước. */
    List<SparePartsIssue> findByWorkOrder_IdOrderByIssuedAtDesc(Integer workOrderId);

    @Query("""
        SELECT s
        FROM SparePartsIssue s
        ORDER BY
            CASE
                WHEN s.status = com.example.m6_thermal_power_plant_api.entity.enums.SparePartsIssueStatus.PENDING
                THEN 0
                ELSE 1
            END,
            s.issuedAt DESC
    """)
    List<SparePartsIssue> findAllOrderByStatusAndIssuedAt();
}
