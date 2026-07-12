package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.SparePartsIssue;
import com.example.m6_thermal_power_plant_api.entity.enums.SparePartsIssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ISparePartsIssueRepository extends JpaRepository<SparePartsIssue, Integer> {

    /** Các phiếu cấp vật tư thay thế của một phiếu công tác, mới nhất trước. */
    List<SparePartsIssue> findByWorkOrder_IdOrderByIssuedAtDesc(Integer workOrderId);

    @Query("""
    SELECT s
    FROM SparePartsIssue s
    LEFT JOIN s.workOrder w
    LEFT JOIN s.issuedBy a
    LEFT JOIN a.employee e
    WHERE
        (
            :keyword IS NULL
            OR LOWER(s.issueCode)
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
            OR s.status = :status
        )
    ORDER BY s.status ASC ,s.issuedAt DESC
""")
    Page<SparePartsIssue> search(
            @Param("keyword") String keyword,
            @Param("status") SparePartsIssueStatus status,
            Pageable pageable
    );
}
