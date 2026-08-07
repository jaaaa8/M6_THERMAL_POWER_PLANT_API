package com.example.m6_thermal_power_plant_api.service.maintenance;

import com.example.m6_thermal_power_plant_api.dto.maintenance.CreateWorkOrderRequest;
import com.example.m6_thermal_power_plant_api.dto.maintenance.RepairRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.StopWorkOrderRequest;
import com.example.m6_thermal_power_plant_api.dto.maintenance.WorkOrderDTO;
import com.example.m6_thermal_power_plant_api.dto.equipment.response.LubricationHistoryDTO;
import com.example.m6_thermal_power_plant_api.entity.*;
import com.example.m6_thermal_power_plant_api.entity.enums.EquipmentStatus;
import com.example.m6_thermal_power_plant_api.entity.enums.RepairPriority;
import com.example.m6_thermal_power_plant_api.entity.enums.RepairRequestStatus;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderEquipmentStatus;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderType;
import com.example.m6_thermal_power_plant_api.exception.DuplicateHumanResourceException;
import com.example.m6_thermal_power_plant_api.exception.ObjectNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.RepairRequestRepository;
import com.example.m6_thermal_power_plant_api.repository.WorkOrderMemberRepository;
import com.example.m6_thermal_power_plant_api.repository.WorkOrderRepository;
import com.example.m6_thermal_power_plant_api.repository.equipment.IEquipmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    private com.example.m6_thermal_power_plant_api.repository.equipment.IEquipmentRepository equipmentRepository;
    @Mock
    private com.example.m6_thermal_power_plant_api.service.pdf.WorkOrderArchiveService workOrderArchiveService;
    @Mock
    private com.example.m6_thermal_power_plant_api.repository.WorkOrderExtensionRepository workOrderExtensionRepository;
    @Mock
    private com.example.m6_thermal_power_plant_api.repository.AccountRepository accountRepository;
    @Mock
    private com.example.m6_thermal_power_plant_api.service.leader.repair_history.IRepairHistoryService repairHistoryService;
    @Mock
    private com.example.m6_thermal_power_plant_api.repository.WorkOrderEquipmentRepository workOrderEquipmentRepository;
    @Mock
    private com.example.m6_thermal_power_plant_api.repository.ILubricationPlanRepository lubricationPlanRepository;
    @Mock
    private com.example.m6_thermal_power_plant_api.service.leader.lubrication.ILubricationHistoryService lubricationHistoryService;
    @Mock
    private com.example.m6_thermal_power_plant_api.service.leader.lubrication_plan.ILubricationPlanService lubricationPlanService;
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
        // Phiếu mới tạo nằm ở Tạm dừng, chờ Trưởng ca mở phiếu ngày.
        assertThat(result.getStatus()).isEqualTo(WorkOrderStatus.STOPPED);
        assertThat(result.getLeaderName()).isEqualTo("Tran Thi Binh");
        assertThat(result.getEquipmentKksCode()).isEqualTo("10LAC10AP001");
        assertThat(result.getMembers()).hasSize(1);
        // MemberInput hiện chỉ nhận employeeId — roleInTask không truyền lúc tạo phiếu.
        assertThat(result.getMembers().get(0).getRoleInTask()).isNull();

        // Tạo PCT xong thì yêu cầu đóng lại: PENDING -> COMPLETED ("đã đóng").
        assertThat(request.getStatus()).isEqualTo(RepairRequestStatus.COMPLETED);
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
    void createWorkOrder_whenActiveWorkOrderHasSameDirectSupervisor_throwsConflict() {
        RepairRequest request = createRequest(2, "RR-2026-0002", RepairRequestStatus.COMPLETED);
        when(repairRequestRepository.findById(2)).thenReturn(Optional.of(request));
        WorkOrder live = liveWorkOrder(1, createEmployee(1, "shift.leader", "Nguyen Van An"),
                LocalDateTime.of(2026, 7, 1, 8, 0));
        when(workOrderRepository.findByRepairRequest_Id(2)).thenReturn(List.of(live));

        // Cùng direct supervisor (id=1) dù giờ KHÔNG đè (ngày khác) -> vẫn bị từ chối.
        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setRepairRequestId(2);
        req.setLeaderId(2);
        req.setDirectSupervisorId(1);
        req.setStartTime(LocalDateTime.of(2026, 7, 2, 8, 0));

        assertThatThrownBy(() -> maintenanceService.createWorkOrderFromRequest(req))
                .isInstanceOf(DuplicateHumanResourceException.class);
        verify(workOrderRepository, never()).save(any(WorkOrder.class));
        verify(repairRequestRepository, never()).save(any(RepairRequest.class));
    }

    @Test
    void createWorkOrder_whenActiveWorkOrderSameLeader_throwsDuplicateHumanResource() {
        RepairRequest request = createRequest(2, "RR-2026-0002", RepairRequestStatus.COMPLETED);
        when(repairRequestRepository.findById(2)).thenReturn(Optional.of(request));
        Employee leader = createEmployee(2, "maintenance.leader", "Tran Thi Binh");
        WorkOrder live = WorkOrder.builder()
                .id(1).orderCode("WO-live-1").status(WorkOrderStatus.IN_PROGRESS)
                .leader(leader)
                .directSupervisor(createEmployee(1, "shift.leader", "Nguyen Van An"))
                .startTime(LocalDateTime.of(2026, 7, 1, 8, 0))
                .build();
        when(workOrderRepository.findByRepairRequest_Id(2)).thenReturn(List.of(live));

        // Cùng leader (id=2) dù khác direct supervisor và giờ không đè -> vẫn bị từ chối.
        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setRepairRequestId(2);
        req.setLeaderId(2);
        req.setDirectSupervisorId(3);
        req.setSafetySupervisorId(4);
        req.setStartTime(LocalDateTime.of(2026, 7, 2, 8, 0));

        assertThatThrownBy(() -> maintenanceService.createWorkOrderFromRequest(req))
                .isInstanceOf(DuplicateHumanResourceException.class);
        verify(workOrderRepository, never()).save(any(WorkOrder.class));
    }

    @Test
    void createWorkOrder_whenActiveWorkOrderSameSafetySupervisor_throwsDuplicateHumanResource() {
        RepairRequest request = createRequest(2, "RR-2026-0002", RepairRequestStatus.COMPLETED);
        when(repairRequestRepository.findById(2)).thenReturn(Optional.of(request));
        Employee safetySupervisor = createEmployee(4, "safety.officer", "Pham Van Dat");
        WorkOrder live = WorkOrder.builder()
                .id(1).orderCode("WO-live-1").status(WorkOrderStatus.IN_PROGRESS)
                .leader(createEmployee(2, "maintenance.leader", "Tran Thi Binh"))
                .directSupervisor(createEmployee(1, "shift.leader", "Nguyen Van An"))
                .safetySupervisor(safetySupervisor)
                .startTime(LocalDateTime.of(2026, 7, 1, 8, 0))
                .build();
        when(workOrderRepository.findByRepairRequest_Id(2)).thenReturn(List.of(live));

        // Cùng safety supervisor (id=4) dù khác leader/direct supervisor và giờ không đè -> vẫn bị từ chối.
        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setRepairRequestId(2);
        req.setLeaderId(5);
        req.setDirectSupervisorId(3);
        req.setSafetySupervisorId(4);
        req.setStartTime(LocalDateTime.of(2026, 7, 2, 8, 0));

        assertThatThrownBy(() -> maintenanceService.createWorkOrderFromRequest(req))
                .isInstanceOf(DuplicateHumanResourceException.class);
        verify(workOrderRepository, never()).save(any(WorkOrder.class));
    }

    @Test
    void createWorkOrder_secondTeamDifferentSupervisorSameHours_isAllowed() {
        RepairRequest request = createRequest(2, "RR-2026-0002", RepairRequestStatus.COMPLETED);
        WorkOrder live = liveWorkOrder(1, createEmployee(3, "electric.tech", "Le Minh Cuong"),
                LocalDateTime.of(2026, 7, 1, 8, 0));
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

        // Khác direct supervisor (1 vs 3) -> cho phép, KỂ CẢ khi trùng khung giờ:
        // từ V13 phiếu không còn mốc kết thúc dự kiến nên không kiểm tra chồng lấn.
        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setRepairRequestId(2);
        req.setLeaderId(2);
        req.setDirectSupervisorId(1);
        req.setStartTime(LocalDateTime.of(2026, 7, 1, 8, 0));

        WorkOrderDTO result = maintenanceService.createWorkOrderFromRequest(req);

        assertThat(result.getId()).isEqualTo(101);
        verify(workOrderRepository).save(any(WorkOrder.class));
    }

    @Test
    void createWorkOrder_existingCancelledWorkOrderIsIgnored_allowsRecreate() {
        RepairRequest request = createRequest(2, "RR-2026-0002", RepairRequestStatus.COMPLETED);
        Employee director = createEmployee(1, "shift.leader", "Nguyen Van An");
        // Phiếu CANCELLED: cùng direct supervisor VÀ cùng khung giờ với phiếu mới,
        // nhưng vì đã huỷ nên phải bị BỎ QUA -> cho phép tạo lại.
        WorkOrder cancelled = WorkOrder.builder()
                .id(1).orderCode("WO-old").status(WorkOrderStatus.CANCELLED)
                .directSupervisor(director)
                .startTime(LocalDateTime.of(2026, 7, 1, 8, 0))
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

        WorkOrderDTO result = maintenanceService.createWorkOrderFromRequest(req);

        assertThat(result.getId()).isEqualTo(102);
        verify(workOrderRepository).save(any(WorkOrder.class));
    }

    /** Tài khoản người tạo phiếu — mọi test huỷ đều so khớp với username này. */
    private static Account creator(String username) {
        Account account = new Account();
        account.setId(9);
        account.setUsername(username);
        return account;
    }

    @Test
    void cancelWorkOrder_setsCancelledAndRevertsRequestToPending_whenNoOtherLiveWorkOrder() {
        RepairRequest request = createRequest(2, "RR-2026-0002", RepairRequestStatus.COMPLETED);
        WorkOrder wo = WorkOrder.builder()
                .id(10).orderCode("WO-x").status(WorkOrderStatus.STOPPED).repairRequest(request)
                .createdBy(creator("team.leader")).build();
        when(workOrderRepository.findById(10)).thenReturn(Optional.of(wo));
        when(workOrderRepository.findByRepairRequest_Id(2)).thenReturn(List.of(wo));

        WorkOrderDTO result = maintenanceService.cancelWorkOrder(10, "team.leader");

        assertThat(wo.getStatus()).isEqualTo(WorkOrderStatus.CANCELLED);
        assertThat(result.getStatus()).isEqualTo(WorkOrderStatus.CANCELLED);
        assertThat(request.getStatus()).isEqualTo(RepairRequestStatus.PENDING);
        verify(workOrderRepository).save(wo);
        verify(repairRequestRepository).save(request);
    }

    @Test
    void cancelWorkOrder_keepsRequestInProgress_whenAnotherLiveWorkOrderExists() {
        RepairRequest request = createRequest(2, "RR-2026-0002", RepairRequestStatus.COMPLETED);
        WorkOrder target = WorkOrder.builder()
                .id(10).orderCode("WO-x").status(WorkOrderStatus.STOPPED).repairRequest(request)
                .createdBy(creator("team.leader")).build();
        WorkOrder otherLive = liveWorkOrder(20, createEmployee(3, "electric.tech", "Le Minh Cuong"),
                LocalDateTime.of(2026, 7, 5, 8, 0));
        when(workOrderRepository.findById(10)).thenReturn(Optional.of(target));
        when(workOrderRepository.findByRepairRequest_Id(2)).thenReturn(List.of(target, otherLive));

        maintenanceService.cancelWorkOrder(10, "team.leader");

        assertThat(target.getStatus()).isEqualTo(WorkOrderStatus.CANCELLED);
        assertThat(request.getStatus()).isEqualTo(RepairRequestStatus.COMPLETED);
        verify(repairRequestRepository, never()).save(any(RepairRequest.class));
    }

    @Test
    void cancelWorkOrder_whenCompleted_throwsConflict() {
        WorkOrder wo = WorkOrder.builder()
                .id(11).orderCode("WO-done").status(WorkOrderStatus.COMPLETED).build();
        when(workOrderRepository.findById(11)).thenReturn(Optional.of(wo));

        assertThatThrownBy(() -> maintenanceService.cancelWorkOrder(11, "team.leader"))
                .isInstanceOf(IllegalStateException.class);
        verify(workOrderRepository, never()).save(any(WorkOrder.class));
    }

    @Test
    void cancelWorkOrder_whenAlreadyRanAWorkDay_throwsConflict() {
        WorkOrder wo = WorkOrder.builder()
                .id(12).orderCode("WO-ran").status(WorkOrderStatus.STOPPED)
                .createdBy(creator("team.leader")).build();
        when(workOrderRepository.findById(12)).thenReturn(Optional.of(wo));
        // Đã chạy 1 ngày công tác → phải đóng bằng "hoàn thành", không được huỷ.
        when(workOrderExtensionRepository.countByWorkOrder_Id(12)).thenReturn(1L);

        assertThatThrownBy(() -> maintenanceService.cancelWorkOrder(12, "team.leader"))
                .isInstanceOf(IllegalStateException.class);
        verify(workOrderRepository, never()).save(any(WorkOrder.class));
    }

    @Test
    void cancelWorkOrder_whenNotTheCreator_throwsAccessDenied() {
        WorkOrder wo = WorkOrder.builder()
                .id(13).orderCode("WO-other").status(WorkOrderStatus.STOPPED)
                .createdBy(creator("team.leader")).build();
        when(workOrderRepository.findById(13)).thenReturn(Optional.of(wo));

        // Đúng role (chặn ở controller) nhưng KHÁC người tạo → vẫn phải từ chối.
        assertThatThrownBy(() -> maintenanceService.cancelWorkOrder(13, "other.leader"))
                .isInstanceOf(AccessDeniedException.class);
        verify(workOrderRepository, never()).save(any(WorkOrder.class));
    }

    @Test
    void cancelWorkOrder_whenNotFound_throws() {
        when(workOrderRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> maintenanceService.cancelWorkOrder(999, "team.leader"))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    void completeWorkOrder_stampsActualEndTime() {
        WorkOrder wo = WorkOrder.builder()
                .id(10).orderCode("WO-x").status(WorkOrderStatus.IN_PROGRESS)
                .startTime(LocalDateTime.of(2026, 7, 1, 8, 0))
                .build();
        when(workOrderRepository.findById(10)).thenReturn(Optional.of(wo));

        LocalDateTime before = LocalDateTime.now();
        maintenanceService.completeWorkOrder(10);

        assertThat(wo.getStatus()).isEqualTo(WorkOrderStatus.COMPLETED);
        assertThat(wo.getEndTime()).isNotNull().isAfterOrEqualTo(before);
    }

    @Test
    void completeWorkOrder_whenAlreadyCompleted_keepsOriginalEndTime() {
        LocalDateTime stamped = LocalDateTime.of(2026, 7, 3, 16, 30);
        WorkOrder wo = WorkOrder.builder()
                .id(10).orderCode("WO-x").status(WorkOrderStatus.COMPLETED)
                .endTime(stamped)
                .build();
        when(workOrderRepository.findById(10)).thenReturn(Optional.of(wo));

        maintenanceService.completeWorkOrder(10);

        assertThat(wo.getEndTime()).isEqualTo(stamped);
    }

    @Test
    void openWorkDay_writesTodayIntoDayLogAndStartsWork() {
        WorkOrder wo = WorkOrder.builder()
                .id(10).orderCode("WO-x").status(WorkOrderStatus.STOPPED).build();
        when(workOrderRepository.findById(10)).thenReturn(Optional.of(wo));
        when(workOrderExtensionRepository
                .findFirstByWorkOrder_IdAndClosedAtIsNullOrderByRequestedAtDesc(10))
                .thenReturn(Optional.empty());

        maintenanceService.openWorkDay(10);

        ArgumentCaptor<WorkOrderExtension> dayCaptor = ArgumentCaptor.forClass(WorkOrderExtension.class);
        verify(workOrderExtensionRepository).save(dayCaptor.capture());
        assertThat(dayCaptor.getValue().getAllowedDate()).isEqualTo(java.time.LocalDate.now());
        assertThat(dayCaptor.getValue().getClosedAt()).isNull();
        assertThat(wo.getStatus()).isEqualTo(WorkOrderStatus.IN_PROGRESS);
    }

    @Test
    void openWorkDay_whenPreviousDayLeftOpen_reusesItInsteadOfCreatingAnother() {
        WorkOrder wo = WorkOrder.builder()
                .id(10).orderCode("WO-x").status(WorkOrderStatus.STOPPED).build();
        when(workOrderRepository.findById(10)).thenReturn(Optional.of(wo));
        when(workOrderExtensionRepository
                .findFirstByWorkOrder_IdAndClosedAtIsNullOrderByRequestedAtDesc(10))
                .thenReturn(Optional.of(WorkOrderExtension.builder().id(1).workOrder(wo).build()));

        maintenanceService.openWorkDay(10);

        verify(workOrderExtensionRepository, never()).save(any(WorkOrderExtension.class));
        assertThat(wo.getStatus()).isEqualTo(WorkOrderStatus.IN_PROGRESS);
    }

    @Test
    void openWorkDay_whenNotStopped_throwsConflict() {
        WorkOrder wo = WorkOrder.builder()
                .id(10).orderCode("WO-x").status(WorkOrderStatus.IN_PROGRESS).build();
        when(workOrderRepository.findById(10)).thenReturn(Optional.of(wo));

        assertThatThrownBy(() -> maintenanceService.openWorkDay(10))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void closeWorkDay_stampsClosedAtWithOptionalNoteAndPausesWorkOrder() {
        WorkOrder wo = WorkOrder.builder()
                .id(10).orderCode("WO-x").status(WorkOrderStatus.IN_PROGRESS).build();
        WorkOrderExtension openDay = WorkOrderExtension.builder().id(1).workOrder(wo).build();
        when(workOrderRepository.findById(10)).thenReturn(Optional.of(wo));
        when(workOrderExtensionRepository
                .findFirstByWorkOrder_IdAndClosedAtIsNullOrderByRequestedAtDesc(10))
                .thenReturn(Optional.of(openDay));

        LocalDateTime before = LocalDateTime.now();
        maintenanceService.closeWorkDay(10, new StopWorkOrderRequest("Het gio lam viec"));

        assertThat(openDay.getClosedAt()).isNotNull().isAfterOrEqualTo(before);
        assertThat(openDay.getReason()).isEqualTo("Het gio lam viec");
        assertThat(wo.getStatus()).isEqualTo(WorkOrderStatus.STOPPED);
    }

    @Test
    void closeWorkDay_withoutNote_stillCloses() {
        WorkOrder wo = WorkOrder.builder()
                .id(10).orderCode("WO-x").status(WorkOrderStatus.IN_PROGRESS).build();
        WorkOrderExtension openDay = WorkOrderExtension.builder().id(1).workOrder(wo).build();
        when(workOrderRepository.findById(10)).thenReturn(Optional.of(wo));
        when(workOrderExtensionRepository
                .findFirstByWorkOrder_IdAndClosedAtIsNullOrderByRequestedAtDesc(10))
                .thenReturn(Optional.of(openDay));

        maintenanceService.closeWorkDay(10, new StopWorkOrderRequest(null));

        assertThat(openDay.getClosedAt()).isNotNull();
        assertThat(openDay.getReason()).isNull();
        assertThat(wo.getStatus()).isEqualTo(WorkOrderStatus.STOPPED);
    }

    @Test
    void completeWorkOrder_closesTheDayStillOpen() {
        WorkOrder wo = WorkOrder.builder()
                .id(10).orderCode("WO-x").status(WorkOrderStatus.IN_PROGRESS).build();
        WorkOrderExtension openDay = WorkOrderExtension.builder().id(1).workOrder(wo).build();
        when(workOrderRepository.findById(10)).thenReturn(Optional.of(wo));
        when(workOrderExtensionRepository
                .findFirstByWorkOrder_IdAndClosedAtIsNullOrderByRequestedAtDesc(10))
                .thenReturn(Optional.of(openDay));

        maintenanceService.completeWorkOrder(10);

        // Nhật ký ngày không được bỏ lửng khi phiếu đã chốt sổ.
        assertThat(openDay.getClosedAt()).isNotNull();
        assertThat(wo.getStatus()).isEqualTo(WorkOrderStatus.COMPLETED);
    }

    /* ── Khoá phiếu hoàn thành → trả thiết bị Sự cố về Hoạt động ───────────── */

    @Test
    void completeWorkOrder_restoresEquipmentToActive_whenNoOtherOpenFault() {
        WorkOrder wo = failureWorkOrder(EquipmentStatus.FAILURE);
        when(workOrderRepository.findById(10)).thenReturn(Optional.of(wo));
        when(repairRequestRepository.existsByEquipment_IdAndStatus(1, RepairRequestStatus.PENDING))
                .thenReturn(false);
        when(workOrderRepository.existsOtherLiveWorkOrderForEquipment(eq(1), eq(10), anyCollection()))
                .thenReturn(false);

        maintenanceService.completeWorkOrder(10);

        assertThat(equipmentOf(wo).getStatus()).isEqualTo(EquipmentStatus.ACTIVE);
    }

    @Test
    void completeWorkOrder_keepsEquipmentInFailure_whenAnotherPendingRequestExists() {
        WorkOrder wo = failureWorkOrder(EquipmentStatus.FAILURE);
        when(workOrderRepository.findById(10)).thenReturn(Optional.of(wo));
        // Còn hư hỏng khác đã báo nhưng chưa cấp phiếu → thiết bị chưa lành.
        when(repairRequestRepository.existsByEquipment_IdAndStatus(1, RepairRequestStatus.PENDING))
                .thenReturn(true);

        maintenanceService.completeWorkOrder(10);

        assertThat(equipmentOf(wo).getStatus()).isEqualTo(EquipmentStatus.FAILURE);
    }

    @Test
    void completeWorkOrder_keepsEquipmentInFailure_whenAnotherLiveWorkOrderExists() {
        WorkOrder wo = failureWorkOrder(EquipmentStatus.FAILURE);
        when(workOrderRepository.findById(10)).thenReturn(Optional.of(wo));
        // Cái bẫy: yêu cầu của phiếu kia đã COMPLETED từ lúc cấp phiếu nên vế
        // PENDING KHÔNG lộ ra — chỉ vế "còn phiếu sống" bắt được.
        when(repairRequestRepository.existsByEquipment_IdAndStatus(1, RepairRequestStatus.PENDING))
                .thenReturn(false);
        when(workOrderRepository.existsOtherLiveWorkOrderForEquipment(eq(1), eq(10), anyCollection()))
                .thenReturn(true);

        maintenanceService.completeWorkOrder(10);

        assertThat(equipmentOf(wo).getStatus()).isEqualTo(EquipmentStatus.FAILURE);
    }

    @Test
    void completeWorkOrder_doesNotTouchEquipmentNotInFailure() {
        // STANDBY là quyết định vận hành khác — khoá phiếu sửa chữa không ghi đè nó.
        WorkOrder wo = failureWorkOrder(EquipmentStatus.STANDBY);
        when(workOrderRepository.findById(10)).thenReturn(Optional.of(wo));

        maintenanceService.completeWorkOrder(10);

        assertThat(equipmentOf(wo).getStatus()).isEqualTo(EquipmentStatus.STANDBY);
        // Thiết bị không ở Sự cố thì thoát sớm, khỏi tốn truy vấn nào.
        verifyNoInteractions(repairRequestRepository);
    }

    /** Phiếu IN_PROGRESS gắn yêu cầu có thiết bị ở trạng thái cho trước. */
    private static WorkOrder failureWorkOrder(EquipmentStatus equipmentStatus) {
        RepairRequest request = createRequest(2, "RR-2026-0002", RepairRequestStatus.COMPLETED);
        request.getEquipment().setStatus(equipmentStatus);
        return WorkOrder.builder()
                .id(10).orderCode("WO-x").status(WorkOrderStatus.IN_PROGRESS)
                .repairRequest(request).build();
    }

    private static Equipment equipmentOf(WorkOrder wo) {
        return wo.getRepairRequest().getEquipment();
    }

    @Test
    void workOrderDto_fromManualWorkOrder_mapsEquipmentList() {
        Equipment e1 = Equipment.builder()
                .id(1).kksCode("KKS-1").name("Quat gio A")
                .system(com.example.m6_thermal_power_plant_api.entity.EquipmentSystem.builder()
                        .id(5).name("He thong nhien lieu").build())
                .build();
        Equipment e2 = Equipment.builder().id(2).kksCode("KKS-2").name("Quat gio B").build();
        WorkOrder wo = WorkOrder.builder()
                .id(7).orderCode("WO-manual").status(WorkOrderStatus.STOPPED)
                .repairDescription("Sua toan bo")
                .workOrderEquipments(List.of(
                        WorkOrderEquipment.builder().equipment(e1)
                                .status(WorkOrderEquipmentStatus.COMPLETED).build(),
                        WorkOrderEquipment.builder().equipment(e2)
                                .status(WorkOrderEquipmentStatus.IN_PROGRESS).build()))
                .build();

        WorkOrderDTO dto = WorkOrderDTO.from(wo, List.of());

        assertThat(dto.getEquipments()).hasSize(2);
        assertThat(dto.getEquipments().get(0).kksCode()).isEqualTo("KKS-1");
        assertThat(dto.getEquipments().get(0).systemName()).isEqualTo("He thong nhien lieu");
        assertThat(dto.getEquipments().get(0).status()).isEqualTo(WorkOrderEquipmentStatus.COMPLETED);
        assertThat(dto.getEquipments().get(1).systemName()).isNull();
        assertThat(dto.getEquipments().get(1).status()).isEqualTo(WorkOrderEquipmentStatus.IN_PROGRESS);
        assertThat(dto.getRepairRequestId()).isNull();
        assertThat(dto.getCreatedAt()).isNotNull(); // bug cũ: chỉ set trong nhánh req != null
    }

    @Test
    void createManualWorkOrder_createsOrderWithMultipleEquipment() {
        Employee leader = createEmployee(2, "maintenance.leader", "Tran Thi Binh");
        Employee technician = createEmployee(5, "mechanic.tech", "Hoang Quoc Dat");
        Equipment e1 = Equipment.builder().id(1).kksCode("KKS-1").name("Quat gio A")
                .system(com.example.m6_thermal_power_plant_api.entity.EquipmentSystem.builder()
                        .id(5).name("He thong nhien lieu").build())
                .build();
        Equipment e2 = Equipment.builder().id(2).kksCode("KKS-2").name("Quat gio B").build();

        when(equipmentRepository.findAllById(List.of(1, 2))).thenReturn(List.of(e1, e2));
        when(workOrderRepository.findLiveHolders(any(), any(), any())).thenReturn(List.of());
        when(employeeRepository.findById(2)).thenReturn(Optional.of(leader));
        when(employeeRepository.findById(5)).thenReturn(Optional.of(technician));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(inv -> {
            WorkOrder wo = inv.getArgument(0);
            wo.setId(200);
            return wo;
        });
        when(workOrderMemberRepository.save(any(WorkOrderMember.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setEquipmentIds(List.of(1, 2));
        req.setLeaderId(2);
        CreateWorkOrderRequest.MemberInput member = new CreateWorkOrderRequest.MemberInput();
        member.setEmployeeId(5);
        req.setMembers(List.of(member));
        req.setRepairDescription("Sua toan bo he thong");

        WorkOrderDTO result = maintenanceService.createManualWorkOrder(req, null);

        assertThat(result.getId()).isEqualTo(200);
        assertThat(result.getOrderCode()).matches("WO-\\d{12}-\\d{3}");
        assertThat(result.getStatus()).isEqualTo(WorkOrderStatus.STOPPED);
        assertThat(result.getRepairRequestId()).isNull();
        assertThat(result.getEquipments()).hasSize(2);
        assertThat(result.getEquipments().get(0).kksCode()).isEqualTo("KKS-1");
        assertThat(result.getEquipments().get(0).status()).isEqualTo(WorkOrderEquipmentStatus.IN_PROGRESS);
        assertThat(result.getLeaderName()).isEqualTo("Tran Thi Binh");
        assertThat(result.getMembers()).hasSize(1);
    }

    /** Id trùng trong payload = bấm nhầm ở UI, không phải xung đột nghiệp vụ → dedupe im lặng. */
    @Test
    void createManualWorkOrder_duplicateEquipmentIds_dedupedSilently() {
        Employee leader = createEmployee(2, "maintenance.leader", "Tran Thi Binh");
        Equipment e1 = Equipment.builder().id(1).kksCode("KKS-1").name("Quat gio A").build();

        when(equipmentRepository.findAllById(List.of(1))).thenReturn(List.of(e1));
        when(workOrderRepository.findLiveHolders(any(), any(), any())).thenReturn(List.of());
        when(employeeRepository.findById(2)).thenReturn(Optional.of(leader));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(inv -> {
            WorkOrder wo = inv.getArgument(0);
            wo.setId(201);
            return wo;
        });

        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setEquipmentIds(List.of(1, 1));
        req.setLeaderId(2);

        WorkOrderDTO result = maintenanceService.createManualWorkOrder(req, null);

        assertThat(result.getEquipments()).hasSize(1);
        assertThat(result.getEquipments().get(0).status()).isEqualTo(WorkOrderEquipmentStatus.IN_PROGRESS);
    }

    @Test
    void createManualWorkOrder_equipmentNotFound_throws() {
        when(equipmentRepository.findAllById(List.of(99))).thenReturn(List.of());

        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setEquipmentIds(List.of(99));
        req.setLeaderId(2);

        assertThatThrownBy(() -> maintenanceService.createManualWorkOrder(req, null))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("99");
        verify(workOrderRepository, never()).save(any(WorkOrder.class));
    }

    /** Chặn cả WO thủ công lẫn WO sinh từ RepairRequest — 1 query duy nhất trả [equipmentId, orderCode]. */
    @Test
    void createManualWorkOrder_equipmentInOtherLiveWorkOrder_throws() {
        Equipment e1 = Equipment.builder().id(1).kksCode("KKS-1").name("Quat gio A").build();
        when(equipmentRepository.findAllById(List.of(1))).thenReturn(List.of(e1));
        when(workOrderRepository.findLiveHolders(eq(List.of(1)), any(), any()))
                .thenReturn(List.<Object[]>of(new Object[]{1, "WO-55"})); // WO-55 dang giu thiet bi 1

        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setEquipmentIds(List.of(1));
        req.setLeaderId(2);

        assertThatThrownBy(() -> maintenanceService.createManualWorkOrder(req, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("KKS-1")
                .hasMessageContaining("WO-55");
        verify(workOrderRepository, never()).save(any(WorkOrder.class));
    }

    /** Một phiếu công tác đang "sống" (IN_PROGRESS) với direct supervisor + giờ bắt đầu cho trước. */
    private static WorkOrder liveWorkOrder(int id, Employee directSupervisor, LocalDateTime start) {
        return WorkOrder.builder()
                .id(id)
                .orderCode("WO-live-" + id)
                .status(WorkOrderStatus.IN_PROGRESS)
                .directSupervisor(directSupervisor)
                .startTime(start)
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

    @Test
    void updateWorkOrderEquipmentStatus_setsCompletedAndReturnsDto() {
        Equipment e1 = Equipment.builder().id(1).kksCode("KKS-1").name("Quat gio A").build();
        WorkOrder wo = WorkOrder.builder()
                .id(7).orderCode("WO-manual").status(WorkOrderStatus.STOPPED)
                .workOrderEquipments(List.of(WorkOrderEquipment.builder().id(11).equipment(e1)
                        .status(WorkOrderEquipmentStatus.IN_PROGRESS).build()))
                .build();
        when(workOrderRepository.findById(7)).thenReturn(Optional.of(wo));
        when(workOrderEquipmentRepository.findByWorkOrder_IdAndEquipment_Id(7, 1))
                .thenReturn(Optional.of(wo.getWorkOrderEquipments().get(0)));
        when(workOrderEquipmentRepository.save(any(WorkOrderEquipment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        WorkOrderDTO result = maintenanceService.updateWorkOrderEquipmentStatus(
                7, 1, WorkOrderEquipmentStatus.COMPLETED);

        assertThat(result.getEquipments().get(0).status())
                .isEqualTo(WorkOrderEquipmentStatus.COMPLETED);
        verify(workOrderEquipmentRepository).save(any(WorkOrderEquipment.class));
    }

    @Test
    void updateWorkOrderEquipmentStatus_requestWorkOrder_throws() {
        RepairRequest req = RepairRequest.builder().id(3).build();
        WorkOrder wo = WorkOrder.builder()
                .id(8).orderCode("WO-req").status(WorkOrderStatus.IN_PROGRESS)
                .repairRequest(req).build();
        when(workOrderRepository.findById(8)).thenReturn(Optional.of(wo));

        assertThatThrownBy(() -> maintenanceService.updateWorkOrderEquipmentStatus(
                8, 1, WorkOrderEquipmentStatus.COMPLETED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("thu cong");
        verify(workOrderEquipmentRepository, never()).save(any(WorkOrderEquipment.class));
    }

    @Test
    void updateWorkOrderEquipmentStatus_completedWorkOrder_throws() {
        WorkOrder wo = WorkOrder.builder()
                .id(9).orderCode("WO-done").status(WorkOrderStatus.COMPLETED).build();
        when(workOrderRepository.findById(9)).thenReturn(Optional.of(wo));

        assertThatThrownBy(() -> maintenanceService.updateWorkOrderEquipmentStatus(
                9, 1, WorkOrderEquipmentStatus.COMPLETED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ket thuc");
    }

    @Test
    void updateWorkOrderEquipmentStatus_cancelStatus_throws() {
        Equipment e1 = Equipment.builder().id(1).kksCode("KKS-1").name("Quat gio A").build();
        WorkOrder wo = WorkOrder.builder()
                .id(10).orderCode("WO-manual").status(WorkOrderStatus.IN_PROGRESS)
                .workOrderEquipments(List.of(WorkOrderEquipment.builder().equipment(e1)
                        .status(WorkOrderEquipmentStatus.IN_PROGRESS).build()))
                .build();
        when(workOrderRepository.findById(10)).thenReturn(Optional.of(wo));

        assertThatThrownBy(() -> maintenanceService.updateWorkOrderEquipmentStatus(
                10, 1, WorkOrderEquipmentStatus.CANCELED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("huy");
    }

    @Test
    void updateWorkOrderEquipmentStatus_equipmentNotInWorkOrder_throws() {
        WorkOrder wo = WorkOrder.builder()
                .id(11).orderCode("WO-manual").status(WorkOrderStatus.IN_PROGRESS)
                .workOrderEquipments(List.of()).build();
        when(workOrderRepository.findById(11)).thenReturn(Optional.of(wo));
        when(workOrderEquipmentRepository.findByWorkOrder_IdAndEquipment_Id(11, 55))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> maintenanceService.updateWorkOrderEquipmentStatus(
                11, 55, WorkOrderEquipmentStatus.COMPLETED))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("55");
    }

    @Test
    void completeWorkOrder_manualWorkOrder_withPendingEquipment_throws() {
        Equipment e1 = Equipment.builder().id(1).kksCode("KKS-1").name("Quat A").build();
        Equipment e2 = Equipment.builder().id(2).kksCode("KKS-2").name("Quat B").build();
        WorkOrder wo = WorkOrder.builder()
                .id(20).orderCode("WO-manual").status(WorkOrderStatus.IN_PROGRESS)
                .workOrderEquipments(List.of(
                        WorkOrderEquipment.builder().equipment(e1)
                                .status(WorkOrderEquipmentStatus.COMPLETED).build(),
                        WorkOrderEquipment.builder().equipment(e2)
                                .status(WorkOrderEquipmentStatus.IN_PROGRESS).build()))
                .build();
        when(workOrderRepository.findById(20)).thenReturn(Optional.of(wo));
        when(workOrderEquipmentRepository.findByWorkOrder_Id(20))
                .thenReturn(wo.getWorkOrderEquipments());

        assertThatThrownBy(() -> maintenanceService.completeWorkOrder(20))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("KKS-2");
        verify(workOrderRepository, never()).save(any(WorkOrder.class));
    }

    @Test
    void completeWorkOrder_manualWorkOrder_allEquipmentCompleted_succeeds() {
        Equipment e1 = Equipment.builder().id(1).kksCode("KKS-1").name("Quat A").build();
        WorkOrder wo = WorkOrder.builder()
                .id(21).orderCode("WO-manual").status(WorkOrderStatus.IN_PROGRESS)
                .workOrderEquipments(List.of(WorkOrderEquipment.builder().equipment(e1)
                        .status(WorkOrderEquipmentStatus.COMPLETED).build()))
                .build();
        when(workOrderRepository.findById(21)).thenReturn(Optional.of(wo));
        when(workOrderEquipmentRepository.findByWorkOrder_Id(21))
                .thenReturn(wo.getWorkOrderEquipments());
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        WorkOrderDTO result = maintenanceService.completeWorkOrder(21);

        assertThat(result.getStatus()).isEqualTo(WorkOrderStatus.COMPLETED);
    }

    @Test
    void cancelWorkOrder_manualWorkOrder_cancelsAllEquipment() {
        Equipment e1 = Equipment.builder().id(1).kksCode("KKS-1").name("Quat A").build();
        WorkOrder wo = WorkOrder.builder()
                .id(22).orderCode("WO-manual").status(WorkOrderStatus.IN_PROGRESS)
                // Huỷ phiếu đòi ĐÚNG người tạo, nên phiếu phải có createdBy.
                .createdBy(creator("team.leader"))
                .workOrderEquipments(List.of(WorkOrderEquipment.builder().equipment(e1)
                        .status(WorkOrderEquipmentStatus.COMPLETED).build()))
                .build();
        when(workOrderRepository.findById(22)).thenReturn(Optional.of(wo));
        when(workOrderEquipmentRepository.findByWorkOrder_Id(22))
                .thenReturn(wo.getWorkOrderEquipments());

        maintenanceService.cancelWorkOrder(22, "team.leader");

        assertThat(wo.getWorkOrderEquipments().get(0).getStatus())
                .isEqualTo(WorkOrderEquipmentStatus.CANCELED);
        verify(workOrderEquipmentRepository).saveAll(any());
    }

    @Test
    void createManualWorkOrder_lubrication_requiresPlanPerLine() {
        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setType(WorkOrderType.LUBRICATION);
        CreateWorkOrderRequest.EquipmentLineInput line = new CreateWorkOrderRequest.EquipmentLineInput();
        line.setEquipmentId(1);
        line.setLubricationPlanId(null);
        req.setEquipmentLines(List.of(line));
        req.setLeaderId(10);
        req.setDirectSupervisorId(11);
        req.setSafetySupervisorId(12);
        req.setStartTime(LocalDateTime.now());

        assertThatThrownBy(() -> maintenanceService.createManualWorkOrder(req, "leader"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("lubricationPlanId");
        verify(workOrderRepository, never()).save(any(WorkOrder.class));
    }

    @Test
    void createManualWorkOrder_lubrication_blockedWhenPlanHasLiveLubricationWoe() {
        Equipment e1 = Equipment.builder().id(1).kksCode("KKS-1").name("Quat gio A").build();
        LubricationPlan plan = LubricationPlan.builder().id(5).lubricationCode("LP-001").equipment(e1).build();
        when(lubricationPlanRepository.findAllById(List.of(5))).thenReturn(List.of(plan));
        when(workOrderEquipmentRepository.existsLiveLubricationWoeByPlanId(eq(5), anyCollection()))
                .thenReturn(true);

        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setType(WorkOrderType.LUBRICATION);
        CreateWorkOrderRequest.EquipmentLineInput line = new CreateWorkOrderRequest.EquipmentLineInput();
        line.setEquipmentId(1);
        line.setLubricationPlanId(5);
        req.setEquipmentLines(List.of(line));
        req.setLeaderId(10);

        assertThatThrownBy(() -> maintenanceService.createManualWorkOrder(req, "leader"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dang co phieu bao duong");
        verify(workOrderRepository, never()).save(any(WorkOrder.class));
    }

    @Test
    void createManualWorkOrder_lubrication_savesWoeWithPlan() {
        Employee leader = createEmployee(2, "maintenance.leader", "Tran Thi Binh");
        Equipment e1 = Equipment.builder().id(1).kksCode("KKS-1").name("Quat gio A").build();
        LubricationPlan plan = LubricationPlan.builder().id(5).lubricationCode("LP-001").equipment(e1).build();
        when(lubricationPlanRepository.findAllById(List.of(5))).thenReturn(List.of(plan));
        when(workOrderEquipmentRepository.existsLiveLubricationWoeByPlanId(eq(5), anyCollection()))
                .thenReturn(false);
        when(employeeRepository.findById(2)).thenReturn(Optional.of(leader));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setType(WorkOrderType.LUBRICATION);
        CreateWorkOrderRequest.EquipmentLineInput line = new CreateWorkOrderRequest.EquipmentLineInput();
        line.setEquipmentId(1);
        line.setLubricationPlanId(5);
        req.setEquipmentLines(List.of(line));
        req.setLeaderId(2);

        maintenanceService.createManualWorkOrder(req, null);

        ArgumentCaptor<WorkOrder> captor = ArgumentCaptor.forClass(WorkOrder.class);
        verify(workOrderRepository).save(captor.capture());
        WorkOrder saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo(WorkOrderType.LUBRICATION);
        assertThat(saved.getWorkOrderEquipments()).hasSize(1);
        assertThat(saved.getWorkOrderEquipments().get(0).getLubricationPlan().getId()).isEqualTo(5);
        assertThat(saved.getWorkOrderEquipments().get(0).getEquipment().getId()).isEqualTo(1);
    }

    @Test
    void completeWorkOrder_lubrication_createsHistoryAndUpdatesPlans() {
        Equipment eq = Equipment.builder().id(1).kksCode("10LAC10AP001").name("Bom dau").build();
        LubricationPlan plan = LubricationPlan.builder().id(5).build();
        WorkOrderEquipment woe = WorkOrderEquipment.builder()
                .id(20).equipment(eq).lubricationPlan(plan)
                .status(WorkOrderEquipmentStatus.COMPLETED).build();
        WorkOrder wo = WorkOrder.builder().id(1).type(WorkOrderType.LUBRICATION)
                .status(WorkOrderStatus.STOPPED).repairDescription("Boi tron dinh ky")
                .workOrderEquipments(List.of(woe)).build();

        when(workOrderRepository.findById(1)).thenReturn(Optional.of(wo));
        when(workOrderEquipmentRepository.findByWorkOrder_Id(1)).thenReturn(List.of(woe));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        maintenanceService.completeWorkOrder(1);

        ArgumentCaptor<LubricationHistoryDTO> captor = ArgumentCaptor.forClass(LubricationHistoryDTO.class);
        verify(lubricationHistoryService).create(captor.capture());
        assertThat(captor.getValue().getEquipmentId()).isEqualTo(1);
        assertThat(captor.getValue().getPerformedDate()).isEqualTo(LocalDate.now());
        assertThat(captor.getValue().getNotes()).isEqualTo("Boi tron dinh ky");
        verify(lubricationPlanService).updateNextDueDateAndStatus(5);
        verify(repairHistoryService, never()).createRepairHistory(any(WorkOrder.class));
    }

    /* ===== Task 1: ràng buộc trùng nhân sự trong-cùng-request ===== */

    @Test
    void createWorkOrderFromRequest_safetySupervisorSameAsLeader_throwsDuplicateHumanResource() {
        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setRepairRequestId(2);
        req.setLeaderId(2);
        req.setSafetySupervisorId(2); // GSAT == leader -> chặn
        req.setStartTime(LocalDateTime.of(2026, 7, 2, 8, 0));

        assertThatThrownBy(() -> maintenanceService.createWorkOrderFromRequest(req))
                .isInstanceOf(DuplicateHumanResourceException.class);
        verify(workOrderRepository, never()).save(any(WorkOrder.class));
    }

    @Test
    void createWorkOrderFromRequest_safetySupervisorSameAsDirectSupervisor_throwsDuplicateHumanResource() {
        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setRepairRequestId(2);
        req.setLeaderId(2);
        req.setDirectSupervisorId(3);
        req.setSafetySupervisorId(3); // GSAT == chi huy truc tiep -> chặn
        req.setStartTime(LocalDateTime.of(2026, 7, 2, 8, 0));

        assertThatThrownBy(() -> maintenanceService.createWorkOrderFromRequest(req))
                .isInstanceOf(DuplicateHumanResourceException.class);
        verify(workOrderRepository, never()).save(any(WorkOrder.class));
    }

    @Test
    void createWorkOrderFromRequest_leaderSameAsDirectSupervisor_isAllowed() {
        RepairRequest request = createRequest(2, "RR-2026-0002", RepairRequestStatus.PENDING);
        Employee leader = createEmployee(2, "maintenance.leader", "Tran Thi Binh");
        Employee safety = createEmployee(4, "safety.officer", "Pham Van Dat");
        when(repairRequestRepository.findById(2)).thenReturn(Optional.of(request));
        when(workOrderRepository.findByRepairRequest_Id(2)).thenReturn(List.of());
        when(employeeRepository.findById(2)).thenReturn(Optional.of(leader));
        when(employeeRepository.findById(4)).thenReturn(Optional.of(safety));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(inv -> {
            WorkOrder wo = inv.getArgument(0);
            wo.setId(101);
            return wo;
        });

        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setRepairRequestId(2);
        req.setLeaderId(2);
        req.setDirectSupervisorId(2); // leader == chi huy truc tiep -> CHO PHEP
        req.setSafetySupervisorId(4);
        req.setStartTime(LocalDateTime.of(2026, 7, 2, 8, 0));

        WorkOrderDTO result = maintenanceService.createWorkOrderFromRequest(req);

        assertThat(result.getId()).isEqualTo(101);
        assertThat(result.getLeaderName()).isEqualTo("Tran Thi Binh");
    }

    @Test
    void createWorkOrderFromRequest_memberDuplicatesRole_throwsDuplicateHumanResource() {
        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setRepairRequestId(2);
        req.setLeaderId(2);           // leader id=2
        req.setDirectSupervisorId(3);
        req.setSafetySupervisorId(4);
        req.setStartTime(LocalDateTime.of(2026, 7, 2, 8, 0));
        CreateWorkOrderRequest.MemberInput member = new CreateWorkOrderRequest.MemberInput();
        member.setEmployeeId(2);      // member trùng leader -> chặn
        req.setMembers(List.of(member));

        assertThatThrownBy(() -> maintenanceService.createWorkOrderFromRequest(req))
                .isInstanceOf(DuplicateHumanResourceException.class);
        verify(workOrderRepository, never()).save(any(WorkOrder.class));
    }

    @Test
    void createWorkOrderFromRequest_duplicateMembers_throwsDuplicateHumanResource() {
        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setRepairRequestId(2);
        req.setLeaderId(2);
        req.setDirectSupervisorId(3);
        req.setSafetySupervisorId(4);
        req.setStartTime(LocalDateTime.of(2026, 7, 2, 8, 0));
        CreateWorkOrderRequest.MemberInput m1 = new CreateWorkOrderRequest.MemberInput();
        m1.setEmployeeId(5);
        CreateWorkOrderRequest.MemberInput m2 = new CreateWorkOrderRequest.MemberInput();
        m2.setEmployeeId(5);          // trùng member -> chặn
        req.setMembers(List.of(m1, m2));

        assertThatThrownBy(() -> maintenanceService.createWorkOrderFromRequest(req))
                .isInstanceOf(DuplicateHumanResourceException.class);
        verify(workOrderRepository, never()).save(any(WorkOrder.class));
    }

    @Test
    void createManualWorkOrder_safetySupervisorSameAsLeader_throwsDuplicateHumanResource() {
        CreateWorkOrderRequest req = new CreateWorkOrderRequest();
        req.setEquipmentIds(List.of(1));
        req.setLeaderId(2);
        req.setSafetySupervisorId(2); // GSAT == leader -> chặn (kiểm tra ngay đầu, chưa cần mock thiết bị)

        assertThatThrownBy(() -> maintenanceService.createManualWorkOrder(req, null))
                .isInstanceOf(DuplicateHumanResourceException.class);
        verify(workOrderRepository, never()).save(any(WorkOrder.class));
    }

    /* ===== Task 2: nhập tay giờ ra/vào + chặn member trùng vai trò của chính phiếu ===== */

    @Test
    void addMember_usesProvidedJoinedAt_whenGiven() {
        WorkOrder wo = liveWorkOrder(1, createEmployee(1, "shift.leader", "Nguyen Van An"),
                LocalDateTime.of(2026, 7, 1, 8, 0));
        Employee tech = createEmployee(5, "mechanic.tech", "Hoang Quoc Dat");
        when(workOrderRepository.findById(1)).thenReturn(Optional.of(wo));
        when(employeeRepository.findById(5)).thenReturn(Optional.of(tech));
        when(workOrderMemberRepository.save(any(WorkOrderMember.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateWorkOrderRequest.MemberInput input = new CreateWorkOrderRequest.MemberInput();
        input.setEmployeeId(5);
        input.setJoinedAt(LocalDateTime.of(2026, 7, 1, 7, 45)); // nhập tay, lệch giờ hiện tại
        maintenanceService.addMember(1, input);

        ArgumentCaptor<WorkOrderMember> captor = ArgumentCaptor.forClass(WorkOrderMember.class);
        verify(workOrderMemberRepository).save(captor.capture());
        assertThat(captor.getValue().getJoinedAt()).isEqualTo(LocalDateTime.of(2026, 7, 1, 7, 45));
    }

    @Test
    void addMember_withoutJoinedAt_defaultsToNow() {
        WorkOrder wo = liveWorkOrder(1, createEmployee(1, "shift.leader", "Nguyen Van An"),
                LocalDateTime.of(2026, 7, 1, 8, 0));
        Employee tech = createEmployee(5, "mechanic.tech", "Hoang Quoc Dat");
        when(workOrderRepository.findById(1)).thenReturn(Optional.of(wo));
        when(employeeRepository.findById(5)).thenReturn(Optional.of(tech));
        when(workOrderMemberRepository.save(any(WorkOrderMember.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateWorkOrderRequest.MemberInput input = new CreateWorkOrderRequest.MemberInput();
        input.setEmployeeId(5);
        maintenanceService.addMember(1, input);

        ArgumentCaptor<WorkOrderMember> captor = ArgumentCaptor.forClass(WorkOrderMember.class);
        verify(workOrderMemberRepository).save(captor.capture());
        assertThat(captor.getValue().getJoinedAt()).isNotNull();
    }

    @Test
    void addMember_whenEmployeeIsRoleHolderOfWorkOrder_throwsDuplicateHumanResource() {
        Employee leader = createEmployee(2, "maintenance.leader", "Tran Thi Binh");
        WorkOrder wo = WorkOrder.builder()
                .id(1).orderCode("WO-live-1").status(WorkOrderStatus.IN_PROGRESS)
                .leader(leader)
                .directSupervisor(createEmployee(1, "shift.leader", "Nguyen Van An"))
                .safetySupervisor(createEmployee(4, "safety.officer", "Pham Van Dat"))
                .startTime(LocalDateTime.of(2026, 7, 1, 8, 0))
                .build();
        when(workOrderRepository.findById(1)).thenReturn(Optional.of(wo));

        CreateWorkOrderRequest.MemberInput input = new CreateWorkOrderRequest.MemberInput();
        input.setEmployeeId(2); // chính là leader của phiếu này -> chặn
        assertThatThrownBy(() -> maintenanceService.addMember(1, input))
                .isInstanceOf(DuplicateHumanResourceException.class);
        verify(workOrderMemberRepository, never()).save(any(WorkOrderMember.class));
    }

    @Test
    void leaveMember_usesProvidedLeftAt_whenGiven() {
        WorkOrder wo = liveWorkOrder(1, createEmployee(1, "shift.leader", "Nguyen Van An"),
                LocalDateTime.of(2026, 7, 1, 8, 0));
        WorkOrderMember member = WorkOrderMember.builder()
                .id(7).workOrder(wo)
                .employees(createEmployee(5, "mechanic.tech", "Hoang Quoc Dat"))
                .joinedAt(LocalDateTime.of(2026, 7, 1, 8, 0))
                .build();
        when(workOrderMemberRepository.findByIdAndWorkOrder_Id(7, 1)).thenReturn(Optional.of(member));

        maintenanceService.leaveMember(1, 7, LocalDateTime.of(2026, 7, 1, 12, 30)); // nhập tay

        assertThat(member.getLeftAt()).isEqualTo(LocalDateTime.of(2026, 7, 1, 12, 30));
        verify(workOrderMemberRepository).save(member);
    }

    @Test
    void leaveMember_withoutLeftAt_defaultsToNow() {
        WorkOrder wo = liveWorkOrder(1, createEmployee(1, "shift.leader", "Nguyen Van An"),
                LocalDateTime.of(2026, 7, 1, 8, 0));
        WorkOrderMember member = WorkOrderMember.builder()
                .id(7).workOrder(wo)
                .employees(createEmployee(5, "mechanic.tech", "Hoang Quoc Dat"))
                .joinedAt(LocalDateTime.of(2026, 7, 1, 8, 0))
                .build();
        when(workOrderMemberRepository.findByIdAndWorkOrder_Id(7, 1)).thenReturn(Optional.of(member));

        maintenanceService.leaveMember(1, 7, null);

        assertThat(member.getLeftAt()).isNotNull();
    }
}
