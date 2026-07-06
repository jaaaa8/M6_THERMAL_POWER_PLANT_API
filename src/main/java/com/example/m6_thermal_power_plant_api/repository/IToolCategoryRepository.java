package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.tool.ToolCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IToolCategoryRepository extends JpaRepository<ToolCategory, Integer> {

    boolean existsByCategoryCode(String categoryCode);

    @Query("SELECT MAX(c.categoryCode) FROM ToolCategory c WHERE c.categoryCode LIKE 'TC%'")
    Optional<String> findMaxCategoryCode();

    @Query(value = """
            SELECT * FROM tool_categories t
            WHERE t.is_deleted = false
              AND (:categoryName = '' OR t.category_name LIKE CONCAT('%', :categoryName, '%'))
              AND (:categoryCode = '' OR t.category_code LIKE CONCAT('%', :categoryCode, '%'))
            """, nativeQuery = true)
    List<ToolCategory> search(@Param("categoryName") String categoryName,
                              @Param("categoryCode") String categoryCode);
}