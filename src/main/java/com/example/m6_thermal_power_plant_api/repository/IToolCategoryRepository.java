package com.example.m6_thermal_power_plant_api.repository;


import com.example.m6_thermal_power_plant_api.entity.tool.ToolCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IToolCategoryRepository extends JpaRepository<ToolCategory, Integer> {
    boolean existsByCategoryCode(String categoryCode);
}