package com.example.m6_thermal_power_plant_api.service.equipment;

import com.example.m6_thermal_power_plant_api.entity.Equipment;
import com.example.m6_thermal_power_plant_api.exception.ObjectNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.EquipmentRepository;
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
class EquipmentManagementServiceTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private SoftDeleteCascadeService softDeleteCascadeService;

    @InjectMocks
    private EquipmentManagementService equipmentManagementService;

    @Test
    void deleteEquipment_cascadeSoftDeletesEquipment() {
        // Arrange
        Equipment equipment = new Equipment();
        equipment.setId(1);
        equipment.setKksCode("KKS-123");
        equipment.setName("Generator");
        equipment.setIsDeleted(false);

        when(equipmentRepository.findById(1)).thenReturn(Optional.of(equipment));
        doAnswer(invocation -> {
            Equipment deletedEquipment = invocation.getArgument(0);
            deletedEquipment.softDelete();
            return null;
        }).when(softDeleteCascadeService).softDelete(equipment);

        // Act
        int deletedEquipmentId = equipmentManagementService.deleteEquipment(1);

        // Assert
        assertThat(deletedEquipmentId).isEqualTo(1);
        assertThat(equipment.getIsDeleted()).isTrue();
        verify(softDeleteCascadeService).softDelete(equipment);
        verify(equipmentRepository, never()).save(equipment);
        verify(equipmentRepository, never()).delete(any(Equipment.class));
    }

    @Test
    void deleteEquipment_whenActiveEquipmentNotFound_throwsException() {
        // Arrange
        when(equipmentRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> equipmentManagementService.deleteEquipment(1))
                .isInstanceOf(ObjectNotFoundException.class);

        verify(softDeleteCascadeService, never()).softDelete(any(Equipment.class));
        verify(equipmentRepository, never()).save(any(Equipment.class));
        verify(equipmentRepository, never()).delete(any(Equipment.class));
    }
}
