package com.example.m6_thermal_power_plant_api.service.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.request.CreateSystemDTO;
import com.example.m6_thermal_power_plant_api.dto.equipment.request.UpdateSystemDTO;
import com.example.m6_thermal_power_plant_api.dto.equipment.response.SystemListDTO;
import com.example.m6_thermal_power_plant_api.entity.EquipmentSystem;
import com.example.m6_thermal_power_plant_api.entity.enums.EquipmentStatus;
import org.springframework.data.domain.Page;

public interface IEquipmentSystemService {

    Page<SystemListDTO> getSystem(
            String name, EquipmentStatus status, int page, int size
    );
    void delete(Integer id);
    SystemListDTO getById(int id);

    SystemListDTO createSystem(CreateSystemDTO dto);

    SystemListDTO updateSystem (int id, UpdateSystemDTO dto);
}
