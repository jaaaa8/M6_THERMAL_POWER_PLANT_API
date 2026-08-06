package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.Equipment;
import com.example.m6_thermal_power_plant_api.entity.EquipmentSystem;
import com.example.m6_thermal_power_plant_api.entity.EquipmentType;
import com.example.m6_thermal_power_plant_api.entity.RepairRequest;
import com.example.m6_thermal_power_plant_api.entity.WorkOrder;
import com.example.m6_thermal_power_plant_api.entity.enums.EquipmentStatus;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus;
import com.example.m6_thermal_power_plant_api.repository.equipment.IEquipmentRepository;
import com.example.m6_thermal_power_plant_api.repository.equipment.IEquipmentSystemRepository;
import com.example.m6_thermal_power_plant_api.repository.equipment.IEquipmentTypeRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class WorkOrderEquipmentTest {

    private static final List<WorkOrderStatus> LIVE = List.of(WorkOrderStatus.IN_PROGRESS);

    @Autowired
    private WorkOrderRepository workOrderRepository;

    @Autowired
    private IEquipmentRepository equipmentRepository;

    @Autowired
    private IEquipmentSystemRepository equipmentSystemRepository;

    @Autowired
    private IEquipmentTypeRepository equipmentTypeRepository;

    @Autowired
    private EntityManager em;

    private EquipmentSystem saveSystem() {
        return equipmentSystemRepository.save(EquipmentSystem.builder()
                .code("SYS-" + UUID.randomUUID().toString().substring(0, 8))
                .name("He thong test")
                .status(EquipmentStatus.ACTIVE)
                .build());
    }

    private EquipmentType saveType() {
        return equipmentTypeRepository.save(EquipmentType.builder()
                .name("Loai test")
                .build());
    }

    private Equipment saveEquipment() {
        EquipmentSystem sys = saveSystem();
        EquipmentType type = saveType();
        return equipmentRepository.save(Equipment.builder()
                .kksCode("EQ-" + UUID.randomUUID().toString().substring(0, 8))
                .name("Thiet bi test")
                .system(sys)
                .equipmentType(type)
                .status(EquipmentStatus.ACTIVE)
                .build());
    }

    private WorkOrder saveWo(String marker, WorkOrderStatus status, List<Equipment> equipments) {
        return workOrderRepository.save(WorkOrder.builder()
                .orderCode("WOTE-" + UUID.randomUUID().toString().substring(0, 12))
                .repairDescription(marker)
                .status(status)
                .equipments(equipments)
                .build());
    }

    @Test
    void liveHolders_findsManualWorkOrder_holdingEquipment() {
        Equipment eq = saveEquipment();
        WorkOrder live = saveWo("live", WorkOrderStatus.IN_PROGRESS, List.of(eq));

        List<Object[]> holders = workOrderRepository.findLiveHolders(List.of(eq.getId()), LIVE);

        assertThat(holders).extracting(row -> row[1]).contains(live.getOrderCode());
    }

    /** Nhánh RepairRequest: thiết bị đang có PCT mở từ phiếu yêu cầu cũng phải bị chặn. */
    @Test
    void liveHolders_findsWorkOrderCreatedFromRepairRequest() {
        Equipment eq = saveEquipment();
        RepairRequest req = RepairRequest.builder()
                .requestCode("RR-" + UUID.randomUUID().toString().substring(0, 8))
                .equipment(eq)
                .build();
        em.persist(req);
        WorkOrder live = workOrderRepository.save(WorkOrder.builder()
                .orderCode("WOTE-" + UUID.randomUUID().toString().substring(0, 12))
                .status(WorkOrderStatus.IN_PROGRESS)
                .repairRequest(req)
                .build());

        List<Object[]> holders = workOrderRepository.findLiveHolders(List.of(eq.getId()), LIVE);

        assertThat(holders).extracting(row -> row[1]).contains(live.getOrderCode());
    }

    @Test
    void liveHolders_empty_whenOnlyCompletedWorkOrderHoldsEquipment() {
        Equipment eq = saveEquipment();
        saveWo("done", WorkOrderStatus.COMPLETED, List.of(eq));

        assertThat(workOrderRepository.findLiveHolders(List.of(eq.getId()), LIVE)).isEmpty();
    }
}