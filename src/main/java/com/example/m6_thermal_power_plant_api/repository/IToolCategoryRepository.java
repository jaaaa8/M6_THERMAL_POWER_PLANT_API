package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.tool.ToolCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IToolCategoryRepository extends JpaRepository<ToolCategory, Integer> {

    boolean existsByCategoryCode(String categoryCode);

    @Query("""
            SELECT t FROM ToolCategory t
            WHERE (:categoryName IS NULL OR :categoryName = ''
                   OR LOWER(t.categoryName) LIKE LOWER(CONCAT('%', :categoryName, '%')))
              AND (:categoryCode IS NULL OR :categoryCode = ''
                   OR LOWER(t.categoryCode) LIKE LOWER(CONCAT('%', :categoryCode, '%')))
            """)
    List<ToolCategory> search(@Param("categoryName") String categoryName,
                              @Param("categoryCode") String categoryCode);
}