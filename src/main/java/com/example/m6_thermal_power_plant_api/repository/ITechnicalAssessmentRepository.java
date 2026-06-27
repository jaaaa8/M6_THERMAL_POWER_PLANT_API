package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.TechnicalAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ITechnicalAssessmentRepository extends JpaRepository<TechnicalAssessment, Integer> {
}
