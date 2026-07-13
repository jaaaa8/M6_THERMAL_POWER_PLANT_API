package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.TechnicalAssessment;
import com.example.m6_thermal_power_plant_api.entity.enums.TechnicalAssessmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ITechnicalAssessmentRepository
        extends JpaRepository<TechnicalAssessment, Integer> {


    TechnicalAssessment findByTechnicalCode(String technicalCode);


    @Query("""
        SELECT t
        FROM TechnicalAssessment t
        WHERE (:technicalCode IS NULL
                OR LOWER(t.technicalCode) LIKE LOWER(CONCAT('%', :technicalCode, '%')))
          AND (:equipmentId IS NULL
                OR t.equipment.id = :equipmentId)
          AND (:status IS NULL
                OR t.status = :status)
        ORDER BY t.status desc , t.createdAt DESC
    """)
    Page<TechnicalAssessment> search(
            @Param("technicalCode") String technicalCode,
            @Param("equipmentId") Integer equipmentId,
            @Param("status") TechnicalAssessmentStatus status,
            Pageable pageable
    );

}