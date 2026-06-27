package com.example.m6_thermal_power_plant_api.service.tool_management;

import com.example.m6_thermal_power_plant_api.entity.tool.Tool;
import com.example.m6_thermal_power_plant_api.exception.ObjectNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.ToolRepository;
import com.example.m6_thermal_power_plant_api.service.soft_delete.SoftDeleteCascadeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ToolManagementServiceTest {

    @Mock
    private ToolRepository toolRepository;

    @Mock
    private SoftDeleteCascadeService softDeleteCascadeService;

    @InjectMocks
    private ToolManagementService toolManagementService;

    @Test
    void deleteTool_cascadeSoftDeletesTool() {
        Tool tool = createTool(false);
        when(toolRepository.findById(1)).thenReturn(Optional.of(tool));
        doAnswer(invocation -> {
            Tool deletedTool = invocation.getArgument(0);
            deletedTool.softDelete();
            return null;
        }).when(softDeleteCascadeService).softDelete(tool);

        int deletedToolId = toolManagementService.deleteTool(1);

        assertThat(deletedToolId).isEqualTo(1);
        assertThat(tool.getIsDeleted()).isTrue();
        verify(softDeleteCascadeService).softDelete(tool);
        verify(toolRepository, never()).save(tool);
        verify(toolRepository, never()).delete(any(Tool.class));
    }

    @Test
    void deleteTool_whenActiveToolNotFound_throwsException() {
        when(toolRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> toolManagementService.deleteTool(1))
                .isInstanceOf(ObjectNotFoundException.class);

        verify(softDeleteCascadeService, never()).softDelete(any(Tool.class));
        verify(toolRepository, never()).save(any(Tool.class));
        verify(toolRepository, never()).delete(any(Tool.class));
    }

    @Test
    void restoreTool_restoresAndSavesTool() {
        Tool tool = createTool(true);
        when(toolRepository.findByIdIncludingDeleted(1)).thenReturn(Optional.of(tool));

        int restoredToolId = toolManagementService.restoreTool(1);

        assertThat(restoredToolId).isEqualTo(1);
        assertThat(tool.getIsDeleted()).isFalse();
        verify(toolRepository).save(tool);
    }

    @Test
    void restoreTool_whenToolDoesNotExist_throwsException() {
        when(toolRepository.findByIdIncludingDeleted(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> toolManagementService.restoreTool(1))
                .isInstanceOf(ObjectNotFoundException.class);

        verify(toolRepository, never()).save(any(Tool.class));
    }

    @Test
    void findById_whenToolFound_returnsTool() {
        Tool tool = createTool(false);
        when(toolRepository.findById(1)).thenReturn(Optional.of(tool));

        Tool foundTool = toolManagementService.findById(1);

        assertThat(foundTool).isNotNull();
        assertThat(foundTool.getId()).isEqualTo(1);
        assertThat(foundTool).isEqualTo(tool);
    }

    @Test
    void findById_whenToolNotFound_throwsException() {
        when(toolRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> toolManagementService.findById(1))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    private static Tool createTool(boolean deleted) {
        Tool tool = new Tool();
        tool.setId(1);
        tool.setToolCode("TL-001");
        tool.setName("Torque wrench");
        tool.setIsDeleted(deleted);
        return tool;
    }
}
