package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.dto.maintenance.WorkOrderDTO;
import com.example.m6_thermal_power_plant_api.entity.Equipment;
import com.example.m6_thermal_power_plant_api.entity.EquipmentSystem;
import com.example.m6_thermal_power_plant_api.entity.EquipmentType;
import com.example.m6_thermal_power_plant_api.entity.RepairRequest;
import com.example.m6_thermal_power_plant_api.entity.WorkOrder;
import com.example.m6_thermal_power_plant_api.entity.WorkOrderEquipment;
import com.example.m6_thermal_power_plant_api.entity.enums.EquipmentStatus;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderEquipmentStatus;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus;
import com.example.m6_thermal_power_plant_api.repository.equipment.IEquipmentRepository;
import com.example.m6_thermal_power_plant_api.repository.equipment.IEquipmentSystemRepository;
import com.example.m6_thermal_power_plant_api.repository.equipment.IEquipmentTypeRepository;
import com.example.m6_thermal_power_plant_api.service.soft_delete.SoftDeleteCascadeService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

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

    @Autowired
    private SoftDeleteCascadeService softDeleteCascadeService;

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
        WorkOrder wo = WorkOrder.builder()
                .orderCode("WOTE-" + UUID.randomUUID().toString().substring(0, 12))
                .repairDescription(marker)
                .status(status)
                .build();
        wo.setWorkOrderEquipments(equipments.stream()
                .<WorkOrderEquipment>map(e -> WorkOrderEquipment.builder()
                        .workOrder(wo)          // ← thiếu = work_order_id NULL = vi phạm NOT NULL (V19)
                        .equipment(e)
                        .status(WorkOrderEquipmentStatus.IN_PROGRESS)
                        .build())
                .toList());
        return workOrderRepository.save(wo);    // cascade = ALL ghi luôn dòng join
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

    @Test
    void workOrderEquipment_savesAndReadsStatus() {
        Equipment eq = saveEquipment();
        WorkOrder wo = saveWo("live", WorkOrderStatus.IN_PROGRESS, List.of(eq));

        em.flush();
        em.clear();

        WorkOrder loaded = workOrderRepository.findById(wo.getId()).orElseThrow();
        assertThat(loaded.getWorkOrderEquipments()).hasSize(1);
        assertThat(loaded.getWorkOrderEquipments().get(0).getStatus())
                .isEqualTo(WorkOrderEquipmentStatus.IN_PROGRESS);
    }

    /** Quyết định 1: xoá mềm thiết bị phải ẩn luôn dòng join, KHÔNG được ném
     *  EntityNotFoundException khi đọc lại WO. Đây là check duy nhất chứng minh
     *  @CascadeSoftDelete hoạt động — thiếu nó thì hồi quy 500 quay lại lặng lẽ. */
    @Test
    void softDeletedEquipment_disappearsFromWorkOrder_withoutThrowing() {
        Equipment eq = saveEquipment();
        WorkOrder wo = saveWo("cascade", WorkOrderStatus.IN_PROGRESS, List.of(eq));
        em.flush();

        softDeleteCascadeService.softDelete(eq);
        em.clear();

        WorkOrder loaded = workOrderRepository.findById(wo.getId()).orElseThrow();
        assertThat(loaded.getWorkOrderEquipments()).isEmpty();
        assertThatNoException().isThrownBy(() -> WorkOrderDTO.from(loaded, List.of()));
    }
}