package com.example.m6_thermal_power_plant_api.service;

import com.example.m6_thermal_power_plant_api.dto.CreateToolDTO;
import com.example.m6_thermal_power_plant_api.entity.Tool;
import com.example.m6_thermal_power_plant_api.entity.ToolCategory;
import com.example.m6_thermal_power_plant_api.repository.IToolCategoryRepository;
import com.example.m6_thermal_power_plant_api.repository.IToolRepository;
import com.example.m6_thermal_power_plant_api.service.impl.IToolService;

import java.util.List;

public class ToolService implements IToolService {
    private IToolRepository toolRepository;
    private IToolCategoryRepository toolCategoryRepository;

    @Override
    public Tool createTool(CreateToolDTO dto) {

        if (toolRepository.existsByToolCode(dto.getToolCode())) {
            throw new RuntimeException("Mã công cụ đã tồn tại");
        }

        ToolCategory category = toolCategoryRepository.findById(dto.getToolCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại công cụ"));

        Tool tool = Tool.builder()
                .toolCode(dto.getToolCode())
                .name(dto.getName())
                .toolCategory(category)
                .quantity(dto.getQuantity())
                .description(dto.getDescription())
                .imgPath(dto.getImgPath())
                .build();

        return toolRepository.save(tool);
    }

    @Override
    public List<Tool> getAllTools() {
        return toolRepository.findAll();
    }

    @Override
    public List<Tool> search(String keyword, Integer categoryId) {
        return toolRepository.search(keyword, categoryId);
    }
}
