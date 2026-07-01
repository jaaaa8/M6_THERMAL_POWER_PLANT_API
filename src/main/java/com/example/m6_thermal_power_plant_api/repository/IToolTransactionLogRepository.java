package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.tool.ToolTransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IToolTransactionLogRepository extends JpaRepository<ToolTransactionLog, Integer> {

    List<ToolTransactionLog> findByToolIdOrderByCreatedAtDesc(Integer toolId);
}
