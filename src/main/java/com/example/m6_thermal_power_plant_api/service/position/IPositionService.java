package com.example.m6_thermal_power_plant_api.service.position;

import com.example.m6_thermal_power_plant_api.dto.employee.PositionDTO;
import java.util.List;

public interface IPositionService {
    List<PositionDTO> getAllPositions();
}
