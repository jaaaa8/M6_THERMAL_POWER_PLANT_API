package com.example.m6_thermal_power_plant_api.service.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.request.UnitDTO;
import com.example.m6_thermal_power_plant_api.dto.equipment.response.UnitListDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IUnitService {
    UnitListDTO getById(int id);
    Page<UnitListDTO> getAll(Pageable pageable);
    UnitListDTO createUnit(UnitDTO dto);
    UnitListDTO updateUnit(Integer id, UnitDTO dto);
    void deleteUnit(int id);
}
