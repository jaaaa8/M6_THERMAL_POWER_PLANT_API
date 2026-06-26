package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.tool.Tool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ToolRepository extends JpaRepository<Tool, Integer> {
    @Query(value = "SELECT * FROM tools WHERE id = :toolId", nativeQuery = true)
    Optional<Tool> findByIdIncludingDeleted(@Param("toolId") Integer toolId);
}
