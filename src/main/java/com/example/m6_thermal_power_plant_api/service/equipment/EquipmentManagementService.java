package com.example.m6_thermal_power_plant_api.service.equipment;

import com.example.m6_thermal_power_plant_api.entity.Equipment;
import com.example.m6_thermal_power_plant_api.exception.ObjectNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.EquipmentRepository;
import com.example.m6_thermal_power_plant_api.service.soft_delete.SoftDeleteCascadeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EquipmentManagementService {

    private final EquipmentRepository equipmentRepository;
    private final SoftDeleteCascadeService softDeleteCascadeService;

    public EquipmentManagementService(EquipmentRepository equipmentRepository, SoftDeleteCascadeService softDeleteCascadeService) {
        this.equipmentRepository = equipmentRepository;
        this.softDeleteCascadeService = softDeleteCascadeService;
    }

    @Transactional
    public int deleteEquipment(Integer equipmentId) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ObjectNotFoundException("Equipment not found"));
        
        softDeleteCascadeService.softDelete(equipment);
        return equipmentId;
    }
}
