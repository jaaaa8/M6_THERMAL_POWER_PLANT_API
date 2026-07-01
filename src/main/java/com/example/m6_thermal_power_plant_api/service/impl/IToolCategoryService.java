package com.example.m6_thermal_power_plant_api.service.impl;

import com.example.m6_thermal_power_plant_api.dto.tool.ToolCategoryRequest;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolCategoryResponse;

import java.util.List;

public interface IToolCategoryService {

    ToolCategoryResponse create(ToolCategoryRequest request);

    ToolCategoryResponse update(Integer id, ToolCategoryRequest request);

    void delete(Integer id);

    ToolCategoryResponse getById(Integer id);

    List<ToolCategoryResponse> getAll();
    List<ToolCategoryResponse> search(String categoryName, String categoryCode);
}
