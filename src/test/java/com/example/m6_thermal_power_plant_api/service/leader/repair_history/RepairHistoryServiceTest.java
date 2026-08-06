package com.example.m6_thermal_power_plant_api.service.leader.repair_history;

import com.example.m6_thermal_power_plant_api.entity.Equipment;
import com.example.m6_thermal_power_plant_api.entity.RepairHistory;
import com.example.m6_thermal_power_plant_api.entity.RepairRequest;
import com.example.m6_thermal_power_plant_api.entity.WorkOrder;
import com.example.m6_thermal_power_plant_api.entity.WorkOrderEquipment;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderEquipmentStatus;
import com.example.m6_thermal_power_plant_api.repository.IRepairHistoryRepository;
import com.example.m6_thermal_power_plant_api.repository.WorkOrderRepository;
import com.example.m6_thermal_power_plant_api.repository.ISparePartRepository;
import com.example.m6_thermal_power_plant_api.repository.equipment.IEquipmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepairHistoryServiceTest {

    @Mock
    private IRepairHistoryRepository repairHistoryRepository;
    @Mock
    private WorkOrderRepository workOrderRepository;
    @Mock
    private IEquipmentRepository equipmentRepository;
    @Mock
    private ISparePartRepository sparePartRepository;
    @InjectMocks
    private RepairHistoryService repairHistoryService;

    @Test
    void createRepairHistory_manualWorkOrder_createsOneRowPerEquipment() {
        Equipment e1 = Equipment.builder().id(1).kksCode("KKS-1").name("Quat A").build();
        Equipment e2 = Equipment.builder().id(2).kksCode("KKS-2").name("Quat B").build();
        WorkOrder wo = WorkOrder.builder()
                .id(10).orderCode("WO-manual")
                .repairDescription("Sua toan bo")
                .workOrderEquipments(List.of(
                        WorkOrderEquipment.builder().equipment(e1).status(WorkOrderEquipmentStatus.IN_PROGRESS).build(),
                        WorkOrderEquipment.builder().equipment(e2).status(WorkOrderEquipmentStatus.IN_PROGRESS).build()))
                .build();
        when(repairHistoryRepository.existsByWorkOrderId(10)).thenReturn(false);
        when(repairHistoryRepository.save(any(RepairHistory.class))).thenAnswer(inv -> inv.getArgument(0));

        repairHistoryService.createRepairHistory(wo);

        ArgumentCaptor<RepairHistory> captor = ArgumentCaptor.forClass(RepairHistory.class);
        verify(repairHistoryRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        List<RepairHistory> saved = captor.getAllValues();
        assertThat(saved).hasSize(2);
        assertThat(saved).extracting(h -> h.getEquipment().getId()).containsExactlyInAnyOrder(1, 2);
        assertThat(saved).allMatch(h -> h.getRepairContent().equals("Sua toan bo"));
    }

    @Test
    void createRepairHistory_requestWorkOrder_createsSingleRow() {
        Equipment e = Equipment.builder().id(1).kksCode("KKS-1").name("Quat A").build();
        RepairRequest req = RepairRequest.builder().id(3).equipment(e).build();
        WorkOrder wo = WorkOrder.builder()
                .id(11).orderCode("WO-req")
                .repairRequest(req)
                .build();
        when(repairHistoryRepository.existsByWorkOrderId(11)).thenReturn(false);
        when(repairHistoryRepository.save(any(RepairHistory.class))).thenAnswer(inv -> inv.getArgument(0));

        repairHistoryService.createRepairHistory(wo);

        ArgumentCaptor<RepairHistory> captor = ArgumentCaptor.forClass(RepairHistory.class);
        verify(repairHistoryRepository).save(captor.capture());
        assertThat(captor.getValue().getEquipment().getId()).isEqualTo(1);
    }
}