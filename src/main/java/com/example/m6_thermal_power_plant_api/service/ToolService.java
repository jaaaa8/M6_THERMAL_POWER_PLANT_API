package com.example.m6_thermal_power_plant_api.service;

import com.example.m6_thermal_power_plant_api.dto.tool.ToolDamageRequest;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolQuantityUpdateRequest;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolRequest;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolResponse;
import com.example.m6_thermal_power_plant_api.entity.tool.Tool;
import com.example.m6_thermal_power_plant_api.entity.tool.ToolCategory;
import com.example.m6_thermal_power_plant_api.exception.BadRequestException;
import com.example.m6_thermal_power_plant_api.exception.ResourceNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.IToolCategoryRepository;
import com.example.m6_thermal_power_plant_api.repository.IToolRepository;
import com.example.m6_thermal_power_plant_api.service.impl.IToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ToolService implements IToolService {

    private final IToolRepository toolRepository;
    private final IToolCategoryRepository toolCategoryRepository;

    @Override
    public ToolResponse create(ToolRequest request) {
        if (toolRepository.existsByToolCode(request.getToolCode())) {
            throw new BadRequestException("Mã CCDC đã tồn tại: " + request.getToolCode());
        }
        ToolCategory category = getCategoryOrThrow(request.getToolCategoryId());

        Tool tool = Tool.builder()
                .toolCode(request.getToolCode())
                .name(request.getName())
                .toolCategory(category)
                .unit(request.getUnit())
                .quantity(request.getQuantity() == null ? 0 : request.getQuantity())
                .note(request.getNote())
                .build();

        return toResponse(toolRepository.save(tool));
    }

    @Override
    public ToolResponse update(Integer id, ToolRequest request) {
        Tool tool = getToolOrThrow(id);

        if (!tool.getToolCode().equals(request.getToolCode())
                && toolRepository.existsByToolCode(request.getToolCode())) {
            throw new BadRequestException("Mã CCDC đã tồn tại: " + request.getToolCode());
        }

        ToolCategory category = getCategoryOrThrow(request.getToolCategoryId());

        tool.setToolCode(request.getToolCode());
        tool.setName(request.getName());
        tool.setToolCategory(category);
        tool.setUnit(request.getUnit());
        tool.setNote(request.getNote());

        return toResponse(toolRepository.save(tool));
    }

    @Override
    public void delete(Integer id) {
        Tool tool = getToolOrThrow(id);
        tool.softDelete();
        toolRepository.save(tool);
    }

    @Override
    public ToolResponse getById(Integer id) {
        return toResponse(getToolOrThrow(id));
    }

    @Override
    public Page<ToolResponse> search(String keyword, Integer categoryId, Pageable pageable) {
        return toolRepository.search(keyword, categoryId, pageable).map(this::toResponse);
    }

    @Override
    public ToolResponse addQuantity(Integer id, ToolQuantityUpdateRequest request) {
        Tool tool = getToolOrThrow(id);
        tool.setQuantity(tool.getQuantity() + request.getQuantity());
        return toResponse(toolRepository.save(tool));
    }

    @Override
    public ToolResponse markDamaged(Integer id, ToolDamageRequest request) {
        Tool tool = getToolOrThrow(id);
        if (request.getQuantity() > tool.getQuantityAvailable()) {
            throw new BadRequestException("Số lượng hư hỏng vượt quá số lượng khả dụng hiện có");
        }
        tool.setQuantityDamaged(tool.getQuantityDamaged() + request.getQuantity());
        if (request.getNote() != null && !request.getNote().isBlank()) {
            tool.setNote(tool.getNote() == null ? request.getNote() : tool.getNote() + "\n" + request.getNote());
        }
        return toResponse(toolRepository.save(tool));
    }

    private Tool getToolOrThrow(Integer id) {
        return toolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy CCDC với id: " + id));
    }

    private ToolCategory getCategoryOrThrow(Integer categoryId) {
        return toolCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chủng loại với id: " + categoryId));
    }

    private ToolResponse toResponse(Tool tool) {
        return ToolResponse.builder()
                .id(tool.getId())
                .toolCode(tool.getToolCode())
                .name(tool.getName())
                .toolCategoryId(tool.getToolCategory().getId())
                .toolCategoryName(tool.getToolCategory().getCategoryName())
                .unit(tool.getUnit())
                .quantity(tool.getQuantity())
                .quantityBorrowed(tool.getQuantityBorrowed())
                .quantityDamaged(tool.getQuantityDamaged())
                .quantityAvailable(tool.getQuantityAvailable())
                .note(tool.getNote())
                .build();
    }
}
