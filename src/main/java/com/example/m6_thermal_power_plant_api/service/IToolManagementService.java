package com.example.m6_thermal_power_plant_api.service;

import com.example.m6_thermal_power_plant_api.entity.tool.Tool;

public interface IToolManagementService {
    int deleteTool(int toolId);
    int restoreTool(int toolId);
    Tool findById(int toolId);
}
