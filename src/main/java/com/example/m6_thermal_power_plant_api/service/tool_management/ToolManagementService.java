package com.example.m6_thermal_power_plant_api.service.tool_management;

import com.example.m6_thermal_power_plant_api.entity.Tool;
import com.example.m6_thermal_power_plant_api.exception.ObjectNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.ToolRepository;
import com.example.m6_thermal_power_plant_api.service.IToolManagementService;
import com.example.m6_thermal_power_plant_api.service.soft_delete.SoftDeleteCascadeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ToolManagementService implements IToolManagementService {
    private final ToolRepository toolRepository;
    private final SoftDeleteCascadeService softDeleteCascadeService;

    public ToolManagementService(ToolRepository toolRepository, SoftDeleteCascadeService softDeleteCascadeService) {
        this.toolRepository = toolRepository;
        this.softDeleteCascadeService = softDeleteCascadeService;
    }

    @Override
    @Transactional
    public int deleteTool(int toolId) {
        Tool tool = getActiveToolOrThrow(toolId);
        softDeleteCascadeService.softDelete(tool);
        return toolId;
    }

    @Override
    @Transactional
    public int restoreTool(int toolId) {
        Tool tool = toolRepository.findByIdIncludingDeleted(toolId)
                .orElseThrow(() -> new ObjectNotFoundException("Khong tim thay cong cu voi id: " + toolId));
        tool.restore();
        toolRepository.save(tool);
        return toolId;
    }

    @Override
    @Transactional(readOnly = true)
    public Tool findById(int toolId) {
        return getActiveToolOrThrow(toolId);
    }

    private Tool getActiveToolOrThrow(int toolId) {
        return toolRepository.findById(toolId)
                .orElseThrow(() -> new ObjectNotFoundException("Khong tim thay cong cu voi id: " + toolId));
    }
}
