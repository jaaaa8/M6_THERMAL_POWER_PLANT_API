package com.example.m6_thermal_power_plant_api.service.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.request.ParameterCatalogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IParameterCatalogService {
    Page<ParameterCatalogDTO> getAll(Pageable pageable);
    ParameterCatalogDTO getById(Integer id);

    ParameterCatalogDTO create(ParameterCatalogDTO dto);

    ParameterCatalogDTO update(Integer id, ParameterCatalogDTO dto);

    void delete(Integer id);
}
