package com.example.m6_thermal_power_plant_api.service.maintenance;

import com.example.m6_thermal_power_plant_api.dto.maintenance.CreateWorkOrderRequest;
import com.example.m6_thermal_power_plant_api.dto.maintenance.RepairRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.WorkOrderDTO;
import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.Employee;
import com.example.m6_thermal_power_plant_api.entity.Equipment;
import com.example.m6_thermal_power_plant_api.entity.RepairRequest;
import com.example.m6_thermal_power_plant_api.entity.WorkOrder;
import com.example.m6_thermal_power_plant_api.entity.WorkOrderMember;
import com.example.m6_thermal_power_plant_api.entity.enums.RepairPriority;
import com.example.m6_thermal_power_plant_api.entity.enums.RepairRequestStatus;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus;
import com.example.m6_thermal_power_plant_api.exception.ObjectNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.AccountRepository;
import com.example.m6_thermal_power_plant_api.repository.RepairRequestRepository;
import com.example.m6_thermal_power_plant_api.repository.WorkOrderMemberRepository;
import com.example.m6_thermal_power_plant_api.repository.WorkOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaintenanceServiceTest {

    @Mock
    private RepairRequestRepository repairRequestRepository;
    @Mock
    private WorkOrderRepository workOrderRepository;
    @Mock
    private WorkOrderMemberRepository workOrderMemberRepository;
    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private MaintenanceService maintenanceService;

    @Test
    void getPendingRepairRequests_mapsEntitiesToDtos() {
        RepairRequest request = createRequest(2, "RR-2026-0002", RepairRequestStatus.PENDING);
        when(repairRequestRepository.findByStatusOrderByCreatedAtDesc(RepairRequestStatus.PENDING))
                .thenReturn(List.of(request));

        List<RepairRequestDTO> result = maintenanceService.getPendingRepairRequests();

        assertThat(result).hasSize(1);
        RepairRequestDTO dto = result.get(0);
        assertThat(dto.getId()).isEqualTo(2);
        assertThat(dto.getRequestCode()).isEqualTo("RR-2026-0002");
        assertThat(dto.getStatus()).isEqualTo(RepairRequestStatus.PENDING);
        assertThat(dto.getEquipmentKksCode()).isEqualTo("10LAC10AP001");
        assertThat(dto.getRequesterName()).isEqualTo("Nguyen Van An");
    }

    @Test
    void createWorkOrderFromRequest_createsOrderTransitionsRequestAndAttachesMembers() {
        RepairRequest request = createRequest(2, "RR-2026-0002", RepairRequestStatus.PENDING);
        Account leader = createAccount(2, "maintenance.leader", "Tran Thi Binh");
        Account technician = createAccount(5, "mechanic.tech", "Hoang Quoc Dat");

        when(repairRequestRepository.findById(2)).thenReturn(Optional.of(request));
        when(workOrderRepository.findByRepairRequest_Id(2)).thenReturn(List.of());
        when(accountRepository.findById(2)).thenReturn(Optional.of(leader));
        when(accountRepository.findById(5)).thenReturn(Optional.of(technician));
        when(workOrderRepository.findFirstByOrderCodeStartingWithOrderByOrderCodeDesc(anyString()))
                .thenReturn(Optional.empty());
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(inv -> {
            WorkOrder wo = inv.getArgument(0);
            wo.setId(100);
            return wo;
        });
        when(workOrderMemberRepository.save(any(WorkOrderMember.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setRepairRequestId(2);
        req.setLeaderId(2);
        CreateWorkOrderRequest.MemberInput member = new CreateWorkOrderRequest.MemberInput();
        member.setAccountId(5);
        member.setRoleInTask("Mechanical technician");
        req.setMembers(List.of(member));

        WorkOrderDTO result = maintenanceService.createWorkOrderFromRequest(req);

        String expectedPrefix = "WO-" + LocalDate.now().getYear() + "-";
        assertThat(result.getId()).isEqualTo(100);
        assertThat(result.getOrderCode()).isEqualTo(expectedPrefix + "0001");
        assertThat(result.getStatus()).isEqualTo(WorkOrderStatus.OPEN);
        assertThat(result.getLeaderName()).isEqualTo("Tran Thi Binh");
        assertThat(result.getEquipmentKksCode()).isEqualTo("10LAC10AP001");
        assertThat(result.getMembers()).hasSize(1);
        assertThat(result.getMembers().get(0).getRoleInTask()).isEqualTo("Mechanical technician");

        // Request rời khỏi danh sách chờ xử lý.
        assertThat(request.getStatus()).isEqualTo(RepairRequestStatus.IN_PROGRESS);
        verify(repairRequestRepository).save(request);

        ArgumentCaptor<WorkOrder> woCaptor = ArgumentCaptor.forClass(WorkOrder.class);
        verify(workOrderRepository).save(woCaptor.capture());
        assertThat(woCaptor.getValue().getRepairRequest()).isSameAs(request);
        assertThat(woCaptor.getValue().getLeader()).isSameAs(leader);
    }

    @Test
    void createWorkOrderFromRequest_whenRequestNotFound_throws() {
        when(repairRequestRepository.findById(99)).thenReturn(Optional.empty());

        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setRepairRequestId(99);
        req.setLeaderId(2);

        assertThatThrownBy(() -> maintenanceService.createWorkOrderFromRequest(req))
                .isInstanceOf(ObjectNotFoundException.class);

        verify(workOrderRepository, never()).save(any(WorkOrder.class));
    }

    @Test
    void createWorkOrderFromRequest_whenRequestAlreadyHasWorkOrder_throwsConflict() {
        RepairRequest request = createRequest(2, "RR-2026-0002", RepairRequestStatus.IN_PROGRESS);
        when(repairRequestRepository.findById(2)).thenReturn(Optional.of(request));
        Account existingLeader = createAccount(2, "maintenance.leader", "Tran Thi Binh");
        when(workOrderRepository.findByRepairRequest_Id(2))
                .thenReturn(List.of(WorkOrder.builder().id(1).leader(existingLeader).build()));

        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setRepairRequestId(2);
        req.setLeaderId(2);

        assertThatThrownBy(() -> maintenanceService.createWorkOrderFromRequest(req))
                .isInstanceOf(IllegalStateException.class);

        verify(workOrderRepository, never()).save(any(WorkOrder.class));
        verify(repairRequestRepository, never()).save(any(RepairRequest.class));
    }

    private static RepairRequest createRequest(int id, String code, RepairRequestStatus status) {
        Equipment equipment = Equipment.builder()
                .id(1)
                .kksCode("10LAC10AP001")
                .name("Boiler Feed Pump A")
                .build();
        Account requester = createAccount(1, "shift.leader", "Nguyen Van An");
        return RepairRequest.builder()
                .id(id)
                .requestCode(code)
                .equipment(equipment)
                .requester(requester)
                .incidentDescription("Abnormal vibration.")
                .priority(RepairPriority.HIGH)
                .status(status)
                .build();
    }

    private static Account createAccount(int id, String username, String fullName) {
        Employee employee = Employee.builder()
                .id(id)
                .employeeCode("EMP-" + id)
                .fullName(fullName)
                .gmail(username + "@example.com")
                .build();
        return Account.builder()
                .id(id)
                .username(username)
                .passwordHash("x")
                .employee(employee)
                .build();
    }
}
