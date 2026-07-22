package com.example.m6_thermal_power_plant_api.service.tool;

import com.example.m6_thermal_power_plant_api.dto.tool.ToolDamageRequest;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolQuantityUpdateRequest;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolRequest;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolResponse;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolTransactionLogResponse;
import com.example.m6_thermal_power_plant_api.entity.enums.ToolTransactionType;
import com.example.m6_thermal_power_plant_api.entity.tool.Tool;
import com.example.m6_thermal_power_plant_api.entity.tool.ToolCategory;
import com.example.m6_thermal_power_plant_api.entity.tool.ToolTransactionLog;
import com.example.m6_thermal_power_plant_api.exception.BadRequestException;
import com.example.m6_thermal_power_plant_api.exception.ResourceNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.IToolCategoryRepository;
import com.example.m6_thermal_power_plant_api.repository.IToolRepository;
import com.example.m6_thermal_power_plant_api.repository.IToolTransactionLogRepository;
import com.example.m6_thermal_power_plant_api.service.impl.IToolService;
import com.example.m6_thermal_power_plant_api.util.TimeStampCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ToolService implements IToolService {

    private final IToolRepository toolRepository;
    private final IToolCategoryRepository toolCategoryRepository;
    private final IToolTransactionLogRepository transactionLogRepository;

    @Override
    public String generateNextCode() {
        // Mã CCDC sinh theo timestamp + hậu tố tự tăng (VD: TO-260714081606-000),
        // luôn duy nhất, không phụ thuộc MAX(tool_code) như cách cũ (dễ trùng).
        return TimeStampCodeGenerator.generate(Tool.class);
    }

    @Override
    public ToolResponse create(ToolRequest request) {
        String code = (request.getToolCode() == null || request.getToolCode().isBlank())
                ? generateNextCode()
                : request.getToolCode().trim();
        if (toolRepository.existsByToolCode(code)) {
            throw new BadRequestException("Mã CCDC đã tồn tại: " + code);
        }
        ToolCategory category = getCategoryOrThrow(request.getToolCategoryId());

        Tool tool = Tool.builder()
                .toolCode(code)
                .name(request.getName())
                .toolCategory(category)
                .unit(request.getUnit())
                .quantity(request.getQuantity() == null ? 0 : request.getQuantity())
                .note(request.getNote())
                .imgPath(request.getImgPath())
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
        tool.setImgPath(request.getImgPath());

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
        String normalizedKeyword = normalize(keyword);
        return toolRepository.search(normalizedKeyword, categoryId, pageable).map(this::toResponse);
    }

    @Override
    public ToolResponse addQuantity(Integer id, ToolQuantityUpdateRequest request) {
        Tool tool = getToolOrThrow(id);
        tool.setQuantity(tool.getQuantity() + request.getQuantity());
        toolRepository.save(tool);
        transactionLogRepository.save(ToolTransactionLog.builder()
                .tool(tool)
                .type(ToolTransactionType.IMPORT)
                .quantity(request.getQuantity())
                .note(request.getNote())
                .build());
        return toResponse(tool);
    }

    @Override
    public ToolResponse markDamaged(Integer id, ToolDamageRequest request) {
        Tool tool = getToolOrThrow(id);
        if (request.getQuantity() > tool.getQuantityAvailable()) {
            throw new BadRequestException("Số lượng hư hỏng vượt quá số lượng khả dụng hiện có");
        }
        tool.setQuantityDamaged(tool.getQuantityDamaged() + request.getQuantity());
        toolRepository.save(tool);
        transactionLogRepository.save(ToolTransactionLog.builder()
                .tool(tool)
                .type(ToolTransactionType.DAMAGE)
                .quantity(request.getQuantity())
                .note(request.getNote())
                .build());
        return toResponse(tool);
    }

    @Override
    public List<ToolTransactionLogResponse> getTransactionLogs(Integer toolId) {
        getToolOrThrow(toolId);
        return transactionLogRepository.findByToolIdOrderByCreatedAtDesc(toolId)
                .stream()
                .map(log -> ToolTransactionLogResponse.builder()
                        .id(log.getId())
                        .type(log.getType())
                        .quantity(log.getQuantity())
                        .note(log.getNote())
                        .createdAt(log.getCreatedAt())
                        .build())
                .toList();
    }

    private Tool getToolOrThrow(Integer id) {
        return toolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy CCDC với id: " + id));
    }

    private ToolCategory getCategoryOrThrow(Integer categoryId) {
        return toolCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chủng loại với id: " + categoryId));
    }

    private String normalize(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        return Normalizer.normalize(keyword.trim(), Normalizer.Form.NFC);
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
                .imgPath(tool.getImgPath())
                .build();
    }
}