package com.example.m6_thermal_power_plant_api.service.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.response.SystemListDTO;
import com.example.m6_thermal_power_plant_api.entity.EquipmentSystem;
import org.springframework.data.domain.Page;

public interface IEquipmentSystemService {

    Page<SystemListDTO> getSystem(
            String keyword, int page, int size
    );
    void delete(Integer id);
}
