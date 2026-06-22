package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.Tool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IToolRepository extends JpaRepository<Tool, Integer> {

    boolean existsByToolCode(String toolCode);

    @Query("""
        SELECT t
        FROM Tool t
        WHERE (:keyword IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:categoryId IS NULL OR t.toolCategory.id = :categoryId)
    """)
    List<Tool> search(String keyword, Integer categoryId);
}