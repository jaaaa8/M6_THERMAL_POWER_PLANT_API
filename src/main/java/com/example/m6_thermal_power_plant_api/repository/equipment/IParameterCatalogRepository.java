package com.example.m6_thermal_power_plant_api.repository.equipment;

import com.example.m6_thermal_power_plant_api.entity.EquipmentParameter;
import com.example.m6_thermal_power_plant_api.entity.ParameterCatalog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IParameterCatalogRepository extends JpaRepository<ParameterCatalog,Integer> {
    boolean existsByNameIgnoreCase(String name);

    @Override
    @EntityGraph(attributePaths = "units")
    Page<ParameterCatalog> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "units")
    Optional<ParameterCatalog> findById(Integer id);
}
