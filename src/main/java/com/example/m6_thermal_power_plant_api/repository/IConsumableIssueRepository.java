package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.ConsumableIssue;
import com.example.m6_thermal_power_plant_api.entity.enums.ConsumableIssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IConsumableIssueRepository extends JpaRepository<ConsumableIssue, Integer> {

    /** Các phiếu cấp vật tư tiêu hao của một phiếu công tác, mới nhất trước. */
    List<ConsumableIssue> findByWorkOrder_IdOrderByIssuedAtDesc(Integer workOrderId);

    @Query("""
    SELECT c
    FROM ConsumableIssue c
    LEFT JOIN c.workOrder w
    LEFT JOIN c.issuedBy a
    LEFT JOIN a.employee e
    WHERE
        (
            :keyword IS NULL
            OR LOWER(c.consumableCode)
                LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(w.orderCode)
                LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(e.fullName)
                LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(a.username)
                LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
    AND
        (
            :status IS NULL
            OR c.status = :status
        )
    ORDER BY c.status ASC ,c.issuedAt DESC
""")
    Page<ConsumableIssue> search(
            @Param("keyword") String keyword,
            @Param("status") ConsumableIssueStatus status,
            Pageable pageable
    );
}
