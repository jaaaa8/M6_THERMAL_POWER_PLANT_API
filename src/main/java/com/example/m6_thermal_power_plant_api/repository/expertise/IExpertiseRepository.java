package com.example.m6_thermal_power_plant_api.repository.expertise;

import com.example.m6_thermal_power_plant_api.entity.Expertise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IExpertiseRepository extends JpaRepository<Expertise, Integer> {
}
