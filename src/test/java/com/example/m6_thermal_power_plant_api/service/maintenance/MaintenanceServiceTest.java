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
import com.example.m6_thermal_power_plant_api.exception.DuplicateHumanResourceException;
import com.example.m6_thermal_power_plant_api.exception.ObjectNotFoundException;
import com.example.m6_thermal_power_plant_api.exception.TimeOverlapException;
import com.example.m6_thermal_power_plant_api.repository.RepairRequestRepository;
import com.example.m6_thermal_power_plant_api.repository.WorkOrderMemberRepository;
import com.example.m6_thermal_power_plant_api.repository.WorkOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private com.example.m6_thermal_power_plant_api.repository.EmployeeRepository employeeRepository;
    @Mock
    private com.example.m6_thermal_power_plant_api.service.spare_part.ISparePartIssuesService sparePartIssuesService;
    @Mock
    private com.example.m6_thermal_power_plant_api.service.pdf.WorkOrderArchiveService workOrderArchiveService;
    @InjectMocks
    private MaintenanceService maintenanceService;

    @Test
    void getPendingRepairRequests_mapsEntitiesToDtos() {
        RepairRequest request = createRequest(2, "RR-2026-0002", RepairRequestStatus.PENDING);
        Pageable pageable = PageRequest.of(0, 20);
        when(repairRequestRepository.findByStatus(eq(RepairRequestStatus.PENDING), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(request), pageable, 1));

        Page<RepairRequestDTO> result = maintenanceService.getPendingRepairRequests(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        RepairRequestDTO dto = result.getContent().get(0);
        assertThat(dto.getId()).isEqualTo(2);
        assertThat(dto.getRequestCode()).isEqualTo("RR-2026-0002");
        assertThat(dto.getStatus()).isEqualTo(RepairRequestStatus.PENDING);
        assertThat(dto.getEquipmentKksCode()).isEqualTo("10LAC10AP001");
        assertThat(dto.getRequesterName()).isEqualTo("Nguyen Van An");
    }

    @Test
    void createWorkOrderFromRequest_createsOrderTransitionsRequestAndAttachesMembers() {
        RepairRequest request = createRequest(2, "RR-2026-0002", RepairRequestStatus.PENDING);
        Employee leader = createEmployee(2, "maintenance.leader", "Tran Thi Binh");
        Employee technician = createEmployee(5, "mechanic.tech", "Hoang Quoc Dat");

        when(repairRequestRepository.findById(2)).thenReturn(Optional.of(request));
        when(workOrderRepository.findByRepairRequest_Id(2)).thenReturn(List.of());
        when(employeeRepository.findById(2)).thenReturn(Optional.of(leader));

        when(employeeRepository.findById(5)).thenReturn(Optional.of(technician));
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
        member.setEmployeeId(5);
        req.setMembers(List.of(member));

        WorkOrderDTO result = maintenanceService.createWorkOrderFromRequest(req);

        assertThat(result.getId()).isEqualTo(100);
        // Mã PCT mới: "WO-" + yyMMddHHmmss (12 chữ số) + "-" + SEQ (3 chữ số).
        assertThat(result.getOrderCode()).matches("WO-\\d{12}-\\d{3}");
        assertThat(result.getStatus()).isEqualTo(WorkOrderStatus.OPEN);
        assertThat(result.getLeaderName()).isEqualTo("Tran Thi Binh");
        assertThat(result.getEquipmentKksCode()).isEqualTo("10LAC10AP001");
        assertThat(result.getMembers()).hasSize(1);
        // MemberInput hiện chỉ nhận employeeId — roleInTask không truyền lúc tạo phiếu.
        assertThat(result.getMembers().get(0).getRoleInTask()).isNull();

        // Chuyển trạng thái PENDING -> IN_PROGRESS đang TẮT trong service
        // (khối comment ở createWorkOrderFromRequest) — request giữ nguyên trạng thái.
        assertThat(request.getStatus()).isEqualTo(RepairRequestStatus.PENDING);
        verify(repairRequestRepository, never()).save(request);

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
    void createWorkOrder_whenActiveWorkOrderHasSameDirectSupervisor_throwsConflict() {
        RepairRequest request = createRequest(2, "RR-2026-0002", RepairRequestStatus.IN_PROGRESS);
        when(repairRequestRepository.findById(2)).thenReturn(Optional.of(request));
        WorkOrder live = liveWorkOrder(1, createEmployee(1, "shift.leader", "Nguyen Van An"),
                LocalDateTime.of(2026, 7, 1, 8, 0), LocalDateTime.of(2026, 7, 1, 12, 0));
        when(workOrderRepository.findByRepairRequest_Id(2)).thenReturn(List.of(live));

        // Cùng direct supervisor (id=1) dù giờ KHÔNG đè (ngày khác) -> vẫn bị từ chối.
        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setRepairRequestId(2);
        req.setLeaderId(2);
        req.setDirectSupervisorId(1);
        req.setStartTime(LocalDateTime.of(2026, 7, 2, 8, 0));
        req.setExpectedEndTime(LocalDateTime.of(2026, 7, 2, 12, 0));

        assertThatThrownBy(() -> maintenanceService.createWorkOrderFromRequest(req))
                .isInstanceOf(DuplicateHumanResourceException.class);
        verify(workOrderRepository, never()).save(any(WorkOrder.class));
        verify(repairRequestRepository, never()).save(any(RepairRequest.class));
    }

    @Test
    void createWorkOrder_whenActiveWorkOrderSameLeader_throwsDuplicateHumanResource() {
        RepairRequest request = createRequest(2, "RR-2026-0002", RepairRequestStatus.IN_PROGRESS);
        when(repairRequestRepository.findById(2)).thenReturn(Optional.of(request));
        Employee leader = createEmployee(2, "maintenance.leader", "Tran Thi Binh");
        WorkOrder live = WorkOrder.builder()
                .id(1).orderCode("WO-live-1").status(WorkOrderStatus.IN_PROGRESS)
                .leader(leader)
                .directSupervisor(createEmployee(1, "shift.leader", "Nguyen Van An"))
                .startTime(LocalDateTime.of(2026, 7, 1, 8, 0))
                .expectedEndTime(LocalDateTime.of(2026, 7, 1, 12, 0))
                .build();
        when(workOrderRepository.findByRepairRequest_Id(2)).thenReturn(List.of(live));

        // Cùng leader (id=2) dù khác direct supervisor và giờ không đè -> vẫn bị từ chối.
        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setRepairRequestId(2);
        req.setLeaderId(2);
        req.setDirectSupervisorId(3);
        req.setSafetySupervisorId(4);
        req.setStartTime(LocalDateTime.of(2026, 7, 2, 8, 0));
        req.setExpectedEndTime(LocalDateTime.of(2026, 7, 2, 12, 0));

        assertThatThrownBy(() -> maintenanceService.createWorkOrderFromRequest(req))
                .isInstanceOf(DuplicateHumanResourceException.class);
        verify(workOrderRepository, never()).save(any(WorkOrder.class));
    }

    @Test
    void createWorkOrder_whenActiveWorkOrderSameSafetySupervisor_throwsDuplicateHumanResource() {
        RepairRequest request = createRequest(2, "RR-2026-0002", RepairRequestStatus.IN_PROGRESS);
        when(repairRequestRepository.findById(2)).thenReturn(Optional.of(request));
        Employee safetySupervisor = createEmployee(4, "safety.officer", "Pham Van Dat");
        WorkOrder live = WorkOrder.builder()
                .id(1).orderCode("WO-live-1").status(WorkOrderStatus.IN_PROGRESS)
                .leader(createEmployee(2, "maintenance.leader", "Tran Thi Binh"))
                .directSupervisor(createEmployee(1, "shift.leader", "Nguyen Van An"))
                .safetySupervisor(safetySupervisor)
                .startTime(LocalDateTime.of(2026, 7, 1, 8, 0))
                .expectedEndTime(LocalDateTime.of(2026, 7, 1, 12, 0))
                .build();
        when(workOrderRepository.findByRepairRequest_Id(2)).thenReturn(List.of(live));

        // Cùng safety supervisor (id=4) dù khác leader/direct supervisor và giờ không đè -> vẫn bị từ chối.
        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setRepairRequestId(2);
        req.setLeaderId(5);
        req.setDirectSupervisorId(3);
        req.setSafetySupervisorId(4);
        req.setStartTime(LocalDateTime.of(2026, 7, 2, 8, 0));
        req.setExpectedEndTime(LocalDateTime.of(2026, 7, 2, 12, 0));

        assertThatThrownBy(() -> maintenanceService.createWorkOrderFromRequest(req))
                .isInstanceOf(DuplicateHumanResourceException.class);
        verify(workOrderRepository, never()).save(any(WorkOrder.class));
    }

    @Test
    void createWorkOrder_whenActiveWorkOrderTimeOverlaps_throwsConflict() {
        RepairRequest request = createRequest(2, "RR-2026-0002", RepairRequestStatus.IN_PROGRESS);
        when(repairRequestRepository.findById(2)).thenReturn(Optional.of(request));
        WorkOrder live = liveWorkOrder(1, createEmployee(3, "electric.tech", "Le Minh Cuong"),
                LocalDateTime.of(2026, 7, 1, 8, 0), LocalDateTime.of(2026, 7, 1, 12, 0));
        when(workOrderRepository.findByRepairRequest_Id(2)).thenReturn(List.of(live));

        // Khác direct supervisor (1 vs 3) nhưng giờ đè lên 08:00-12:00 -> bị từ chối.
        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setRepairRequestId(2);
        req.setLeaderId(2);
        req.setDirectSupervisorId(1);
        req.setStartTime(LocalDateTime.of(2026, 7, 1, 10, 0));
        req.setExpectedEndTime(LocalDateTime.of(2026, 7, 1, 14, 0));

        assertThatThrownBy(() -> maintenanceService.createWorkOrderFromRequest(req))
                .isInstanceOf(TimeOverlapException.class);
        verify(workOrderRepository, never()).save(any(WorkOrder.class));
    }

    @Test
    void createWorkOrder_secondTeamDifferentSupervisorAndNoOverlap_isAllowed() {
        RepairRequest request = createRequest(2, "RR-2026-0002", RepairRequestStatus.IN_PROGRESS);
        WorkOrder live = liveWorkOrder(1, createEmployee(3, "electric.tech", "Le Minh Cuong"),
                LocalDateTime.of(2026, 7, 1, 8, 0), LocalDateTime.of(2026, 7, 1, 12, 0));
        Employee leader = createEmployee(2, "maintenance.leader", "Tran Thi Binh");
        Employee newDirect = createEmployee(1, "shift.leader", "Nguyen Van An");

        when(repairRequestRepository.findById(2)).thenReturn(Optional.of(request));
        when(workOrderRepository.findByRepairRequest_Id(2)).thenReturn(List.of(live));
        when(employeeRepository.findById(2)).thenReturn(Optional.of(leader));
        when(employeeRepository.findById(1)).thenReturn(Optional.of(newDirect));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(inv -> {
            WorkOrder wo = inv.getArgument(0);
            wo.setId(101);
            return wo;
        });

        // Khác direct supervisor (1 vs 3) VÀ giờ bắt đầu (13:00) sau khi phiếu cũ kết thúc (12:00).
        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setRepairRequestId(2);
        req.setLeaderId(2);
        req.setDirectSupervisorId(1);
        req.setStartTime(LocalDateTime.of(2026, 7, 1, 13, 0));
        req.setExpectedEndTime(LocalDateTime.of(2026, 7, 1, 17, 0));

        WorkOrderDTO result = maintenanceService.createWorkOrderFromRequest(req);

        assertThat(result.getId()).isEqualTo(101);
        verify(workOrderRepository).save(any(WorkOrder.class));
    }

    @Test
    void createWorkOrder_existingCancelledWorkOrderIsIgnored_allowsRecreate() {
        RepairRequest request = createRequest(2, "RR-2026-0002", RepairRequestStatus.IN_PROGRESS);
        Employee director = createEmployee(1, "shift.leader", "Nguyen Van An");
        // Phiếu CANCELLED: cùng direct supervisor VÀ cùng khung giờ với phiếu mới,
        // nhưng vì đã huỷ nên phải bị BỎ QUA -> cho phép tạo lại.
        WorkOrder cancelled = WorkOrder.builder()
                .id(1).orderCode("WO-old").status(WorkOrderStatus.CANCELLED)
                .directSupervisor(director)
                .startTime(LocalDateTime.of(2026, 7, 1, 8, 0))
                .expectedEndTime(LocalDateTime.of(2026, 7, 1, 12, 0))
                .build();
        Employee leader = createEmployee(2, "maintenance.leader", "Tran Thi Binh");

        when(repairRequestRepository.findById(2)).thenReturn(Optional.of(request));
        when(workOrderRepository.findByRepairRequest_Id(2)).thenReturn(List.of(cancelled));
        when(employeeRepository.findById(2)).thenReturn(Optional.of(leader));
        when(employeeRepository.findById(1)).thenReturn(Optional.of(director));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(inv -> {
            WorkOrder wo = inv.getArgument(0);
            wo.setId(102);
            return wo;
        });

        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setRepairRequestId(2);
        req.setLeaderId(2);
        req.setDirectSupervisorId(1);
        req.setStartTime(LocalDateTime.of(2026, 7, 1, 8, 0));
        req.setExpectedEndTime(LocalDateTime.of(2026, 7, 1, 12, 0));

        WorkOrderDTO result = maintenanceService.createWorkOrderFromRequest(req);

        assertThat(result.getId()).isEqualTo(102);
        verify(workOrderRepository).save(any(WorkOrder.class));
    }

    @Test
    void cancelWorkOrder_setsCancelledAndRevertsRequestToPending_whenNoOtherLiveWorkOrder() {
        RepairRequest request = createRequest(2, "RR-2026-0002", RepairRequestStatus.IN_PROGRESS);
        WorkOrder wo = WorkOrder.builder()
                .id(10).orderCode("WO-x").status(WorkOrderStatus.OPEN).repairRequest(request).build();
        when(workOrderRepository.findById(10)).thenReturn(Optional.of(wo));
        when(workOrderRepository.findByRepairRequest_Id(2)).thenReturn(List.of(wo));

        WorkOrderDTO result = maintenanceService.cancelWorkOrder(10);

        assertThat(wo.getStatus()).isEqualTo(WorkOrderStatus.CANCELLED);
        assertThat(result.getStatus()).isEqualTo(WorkOrderStatus.CANCELLED);
        assertThat(request.getStatus()).isEqualTo(RepairRequestStatus.PENDING);
        verify(workOrderRepository).save(wo);
        verify(repairRequestRepository).save(request);
    }

    @Test
    void cancelWorkOrder_keepsRequestInProgress_whenAnotherLiveWorkOrderExists() {
        RepairRequest request = createRequest(2, "RR-2026-0002", RepairRequestStatus.IN_PROGRESS);
        WorkOrder target = WorkOrder.builder()
                .id(10).orderCode("WO-x").status(WorkOrderStatus.OPEN).repairRequest(request).build();
        WorkOrder otherLive = liveWorkOrder(20, createEmployee(3, "electric.tech", "Le Minh Cuong"),
                LocalDateTime.of(2026, 7, 5, 8, 0), LocalDateTime.of(2026, 7, 5, 12, 0));
        when(workOrderRepository.findById(10)).thenReturn(Optional.of(target));
        when(workOrderRepository.findByRepairRequest_Id(2)).thenReturn(List.of(target, otherLive));

        maintenanceService.cancelWorkOrder(10);

        assertThat(target.getStatus()).isEqualTo(WorkOrderStatus.CANCELLED);
        assertThat(request.getStatus()).isEqualTo(RepairRequestStatus.IN_PROGRESS);
        verify(repairRequestRepository, never()).save(any(RepairRequest.class));
    }

    @Test
    void cancelWorkOrder_whenCompleted_throwsConflict() {
        WorkOrder wo = WorkOrder.builder()
                .id(11).orderCode("WO-done").status(WorkOrderStatus.COMPLETED).build();
        when(workOrderRepository.findById(11)).thenReturn(Optional.of(wo));

        assertThatThrownBy(() -> maintenanceService.cancelWorkOrder(11))
                .isInstanceOf(IllegalStateException.class);
        verify(workOrderRepository, never()).save(any(WorkOrder.class));
    }

    @Test
    void cancelWorkOrder_whenNotFound_throws() {
        when(workOrderRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> maintenanceService.cancelWorkOrder(999))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    /** Một phiếu công tác đang "sống" (IN_PROGRESS) với direct supervisor + khung giờ cho trước. */
    private static WorkOrder liveWorkOrder(int id, Employee directSupervisor, LocalDateTime start, LocalDateTime end) {
        return WorkOrder.builder()
                .id(id)
                .orderCode("WO-live-" + id)
                .status(WorkOrderStatus.IN_PROGRESS)
                .directSupervisor(directSupervisor)
                .startTime(start)
                .expectedEndTime(end)
                .build();
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

    /** Employee dùng cho leader / directSupervisor / safetySupervisor của WorkOrder (KHÔNG phải Account). */
    private static Employee createEmployee(int id, String code, String fullName) {
        return Employee.builder()
                .id(id)
                .employeeCode("EMP-" + code)
                .fullName(fullName)
                .gmail(code + "@example.com")
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
