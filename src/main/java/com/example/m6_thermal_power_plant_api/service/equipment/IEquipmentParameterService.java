package com.example.m6_thermal_power_plant_api.service.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.request.ParameterDTO;

import java.util.List;

public interface IEquipmentParameterService {
        List<ParameterDTO> getByEquipment(Integer equipmentId);


        List<ParameterDTO> create(
                List<ParameterDTO> dtos
        );


        ParameterDTO update(
                Integer id,
                ParameterDTO dto
        );


        void delete(Integer id);
}
