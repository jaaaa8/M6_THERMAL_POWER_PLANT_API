package com.example.m6_thermal_power_plant_api.service.impl;


import com.example.m6_thermal_power_plant_api.dto.tool.ToolDamageRequest;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolQuantityUpdateRequest;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolRequest;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolResponse;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolTransactionLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IToolService {

    ToolResponse create(ToolRequest request);

    ToolResponse update(Integer id, ToolRequest request);

    void delete(Integer id);

    ToolResponse getById(Integer id);

    Page<ToolResponse> search(String keyword, Integer categoryId, Pageable pageable);

    ToolResponse addQuantity(Integer id, ToolQuantityUpdateRequest request);

    ToolResponse markDamaged(Integer id, ToolDamageRequest request);

    List<ToolTransactionLogResponse> getTransactionLogs(Integer toolId);

    String generateNextCode();
}