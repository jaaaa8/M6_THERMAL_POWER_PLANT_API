package com.example.m6_thermal_power_plant_api.service;

import com.example.m6_thermal_power_plant_api.dto.tool.ToolCategoryRequest;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolCategoryResponse;
import com.example.m6_thermal_power_plant_api.entity.tool.ToolCategory;
import com.example.m6_thermal_power_plant_api.exception.BadRequestException;
import com.example.m6_thermal_power_plant_api.exception.ResourceNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.IToolCategoryRepository;
import com.example.m6_thermal_power_plant_api.service.impl.IToolCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ToolCategoryService implements IToolCategoryService {

    private final IToolCategoryRepository toolCategoryRepository;

    @Override
    public ToolCategoryResponse create(ToolCategoryRequest request) {
        if (toolCategoryRepository.existsByCategoryCode(request.getCategoryCode())) {
            throw new BadRequestException("Mã chủng loại đã tồn tại: " + request.getCategoryCode());
        }
        ToolCategory category = ToolCategory.builder()
                .categoryCode(request.getCategoryCode())
                .categoryName(request.getCategoryName())
                .description(request.getDescription())
                .build();
        return toResponse(toolCategoryRepository.save(category));
    }

    @Override
    public ToolCategoryResponse update(Integer id, ToolCategoryRequest request) {
        ToolCategory category = getOrThrow(id);
        if (!category.getCategoryCode().equals(request.getCategoryCode())
                && toolCategoryRepository.existsByCategoryCode(request.getCategoryCode())) {
            throw new BadRequestException("Mã chủng loại đã tồn tại: " + request.getCategoryCode());
        }
        category.setCategoryCode(request.getCategoryCode());
        category.setCategoryName(request.getCategoryName());
        category.setDescription(request.getDescription());
        return toResponse(toolCategoryRepository.save(category));
    }

    @Override
    public void delete(Integer id) {
        ToolCategory category = getOrThrow(id);
        category.softDelete();
        toolCategoryRepository.save(category);
    }

    @Override
    public ToolCategoryResponse getById(Integer id) {
        return toResponse(getOrThrow(id));
    }

    @Override
    public List<ToolCategoryResponse> getAll() {
        return toolCategoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public List<ToolCategoryResponse> search(String categoryName, String categoryCode) {
        String safeName = StringUtils.hasText(categoryName) ? categoryName.trim() : null;
        String safeCode = StringUtils.hasText(categoryCode) ? categoryCode.trim() : null;

        return toolCategoryRepository.search(safeName, safeCode)
                .stream()
                .map(this::toResponse)
                .toList();
    }
    private ToolCategory getOrThrow(Integer id) {
        return toolCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chủng loại với id: " + id));
    }

    private ToolCategoryResponse toResponse(ToolCategory category) {
        return ToolCategoryResponse.builder()
                .id(category.getId())
                .categoryCode(category.getCategoryCode())
                .categoryName(category.getCategoryName())
                .description(category.getDescription())
                .build();
    }
}
