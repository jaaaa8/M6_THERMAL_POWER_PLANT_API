package com.example.m6_thermal_power_plant_api.service.equipment;

import com.example.m6_thermal_power_plant_api.entity.Equipment;
import com.example.m6_thermal_power_plant_api.repository.EquipmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class EquipmentManagementServiceDbTest {

    @Autowired
    private EquipmentManagementService equipmentManagementService;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Test
    @Transactional
    @Commit
    void deleteEquipment_andCommitToDatabase() {
        // 1. Create and save a new equipment
        Equipment equipment = new Equipment();
        equipment.setKksCode("KKS-TEST-001");
        equipment.setName("Test Equipment for DB Check");
        equipment.setStatus("Đang vận hành");
        equipment.setIsDeleted(false);
        
        equipment = equipmentRepository.save(equipment);
        Integer newEquipmentId = equipment.getId();
        System.out.println("Created new equipment with ID: " + newEquipmentId);

        // 2. Delete the equipment using the new method
        equipmentManagementService.deleteEquipment(newEquipmentId);
        System.out.println("Deleted equipment with ID: " + newEquipmentId);

        // 3. Verify it is marked as deleted
        Equipment deletedEquipment = equipmentRepository.findByIdIncludingDeleted(newEquipmentId)
                .orElseThrow(() -> new RuntimeException("Equipment completely removed, should be soft-deleted!"));
        
        assertThat(deletedEquipment.getIsDeleted()).isTrue();
        System.out.println("Equipment isDeleted status in Java: " + deletedEquipment.getIsDeleted());
        System.out.println("Check your MySQL database for equipment ID " + newEquipmentId + ", is_deleted should be 1.");
    }
}
