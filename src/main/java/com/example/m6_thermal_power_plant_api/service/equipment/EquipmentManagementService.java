package com.example.m6_thermal_power_plant_api.service.equipment;

import com.example.m6_thermal_power_plant_api.entity.Equipment;
import com.example.m6_thermal_power_plant_api.exception.ObjectNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.IEquipmentRepository;
import com.example.m6_thermal_power_plant_api.service.soft_delete.SoftDeleteCascadeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EquipmentManagementService {

    private final IEquipmentRepository equipmentRepository;
    private final SoftDeleteCascadeService softDeleteCascadeService;

    public EquipmentManagementService(IEquipmentRepository equipmentRepository, SoftDeleteCascadeService softDeleteCascadeService) {
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

    /**
     * Khôi phục thiết bị đã bị xoá mềm (và toàn bộ dependent đã bị ẩn theo nó).
     * Dùng {@code findByIdIncludingDeleted} vì thiết bị đang ở trạng thái
     * is_deleted = true nên {@code findById} (bị @SQLRestriction lọc) sẽ không thấy.
     */
    @Transactional
    public int restoreEquipment(Integer equipmentId) {
        Equipment equipment = equipmentRepository.findByIdIncludingDeleted(equipmentId)
                .orElseThrow(() -> new ObjectNotFoundException("Equipment not found"));

        softDeleteCascadeService.restore(equipment);
        return equipmentId;
    }
}
