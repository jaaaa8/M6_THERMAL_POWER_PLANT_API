package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.LubricationPlan;
import com.example.m6_thermal_power_plant_api.entity.enums.LubricationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ILubricationPlanRepository extends JpaRepository<LubricationPlan, Integer> {
    @Query("""
        SELECT lp
        FROM LubricationPlan lp
        LEFT JOIN lp.equipment e
        LEFT JOIN lp.consumable c
        WHERE
        (
            :keyword IS NULL
            OR :keyword = ''
            OR LOWER(lp.lubricationCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(e.kksCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(c.consumableCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        AND
        (
            :status IS NULL
            OR lp.status = :status
        )
    """)
    Page<LubricationPlan> search(
            String keyword,
            LubricationStatus status,
            Pageable pageable
    );
}
