package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.LubricationPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ILubricationPlanRepository extends JpaRepository<LubricationPlan, Integer> {
}
