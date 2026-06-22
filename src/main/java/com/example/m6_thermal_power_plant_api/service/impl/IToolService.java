package com.example.m6_thermal_power_plant_api.service.impl;

import com.example.m6_thermal_power_plant_api.dto.CreateToolDTO;
import com.example.m6_thermal_power_plant_api.entity.Tool;

import java.util.List;

public interface IToolService {
    Tool createTool(CreateToolDTO dto);

    List<Tool> getAllTools();

    List<Tool> search(String keyword, Integer categoryId);
}
