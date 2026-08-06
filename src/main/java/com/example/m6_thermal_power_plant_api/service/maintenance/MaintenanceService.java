package com.example.m6_thermal_power_plant_api.service.maintenance;

import com.example.m6_thermal_power_plant_api.dto.maintenance.CreateWorkOrderRequest;
import com.example.m6_thermal_power_plant_api.dto.maintenance.MemberHistoryEventDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.RepairRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.StopWorkOrderRequest;
import com.example.m6_thermal_power_plant_api.dto.maintenance.UpdateWorkOrderRequest;
import com.example.m6_thermal_power_plant_api.dto.maintenance.UpdateWorkOrderStatusRequest;
import com.example.m6_thermal_power_plant_api.dto.maintenance.WorkOrderDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.WorkOrderDetailDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.WorkOrderExtensionDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.WorkOrderMemberDTO;
import com.example.m6_thermal_power_plant_api.entity.*;
import com.example.m6_thermal_power_plant_api.entity.enums.EquipmentStatus;
import com.example.m6_thermal_power_plant_api.entity.enums.RepairRequestStatus;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderEquipmentStatus;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus;
import com.example.m6_thermal_power_plant_api.exception.DuplicateHumanResourceException;
import com.example.m6_thermal_power_plant_api.exception.ObjectNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.*;
import com.example.m6_thermal_power_plant_api.service.leader.repair_history.IRepairHistoryService;
import com.example.m6_thermal_power_plant_api.service.pdf.WorkOrderArchiveService;
import com.example.m6_thermal_power_plant_api.util.TimeStampCodeGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MaintenanceService implements IMaintenanceService {

    private final RepairRequestRepository repairRequestRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderMemberRepository workOrderMemberRepository;
    private final WorkOrderExtensionRepository workOrderExtensionRepository;
    private final EmployeeRepository employeeRepository;
    private final AccountRepository accountRepository;
    private final WorkOrderArchiveService workOrderArchiveService;
    private final IRepairHistoryService repairHistoryService;
    private final WorkOrderEquipmentRepository workOrderEquipmentRepository;

    private final com.example.m6_thermal_power_plant_api.repository.equipment.IEquipmentRepository equipmentRepository;

    public MaintenanceService(WorkOrderRepository workOrderRepository,
                              RepairRequestRepository repairRequestRepository,
                              WorkOrderMemberRepository workOrderMemberRepository,
                              WorkOrderExtensionRepository workOrderExtensionRepository,
                              EmployeeRepository employeeRepository,
                              com.example.m6_thermal_power_plant_api.repository.equipment.IEquipmentRepository equipmentRepository,
                              AccountRepository accountRepository,
                              WorkOrderArchiveService workOrderArchiveService,IRepairHistoryService repairHistoryService,
                              WorkOrderEquipmentRepository workOrderEquipmentRepository) {
        this.workOrderRepository = workOrderRepository;
        this.repairRequestRepository = repairRequestRepository;
        this.workOrderMemberRepository = workOrderMemberRepository;
        this.workOrderExtensionRepository = workOrderExtensionRepository;
        this.employeeRepository = employeeRepository;
        this.equipmentRepository = equipmentRepository;
        this.accountRepository = accountRepository;
        this.workOrderArchiveService = workOrderArchiveService;
        this.repairHistoryService = repairHistoryService;
        this.workOrderEquipmentRepository = workOrderEquipmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RepairRequestDTO> getPendingRepairRequests(Pageable pageable) {
        // Page.map giữ nguyên metadata phân trang; RepairRequestDTO.from chạy
        // TRONG transaction readOnly nên các quan hệ LAZY map được an toàn.
        return repairRequestRepository
                .findByStatus(RepairRequestStatus.PENDING, pageable)
                .map(RepairRequestDTO::from);
    }

    @Override
    @Transactional
    public WorkOrderDTO createWorkOrderFromRequest(CreateWorkOrderRequest request, String createdByUsername) {
        RepairRequest repairRequest = repairRequestRepository.findById(request.getRepairRequestId())
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Khong tim thay yeu cau sua chua voi id: " + request.getRepairRequestId()));

        Account createdBy = createdByUsername == null ? null
                : accountRepository.findAccountByUsername(createdByUsername)
                        .orElseThrow(() -> new ObjectNotFoundException(
                                "Khong tim thay tai khoan dang nhap: " + createdByUsername));

        validateActiveWorkOrderConstraints(repairRequest, request);

        Employee leader = loadEmployee(request.getLeaderId(), "nguoi lanh dao cong viec");
        Employee directSupervisor = loadEmployeeOrNull(request.getDirectSupervisorId(), "chi huy truc tiep");
        Employee safetySupervisor = loadEmployeeOrNull(request.getSafetySupervisorId(), "nguoi giam sat an toan");

        // Mô tả sửa chữa: mặc định lấy từ mô tả sự cố của yêu cầu; người tạo
        // sửa lại được thì dùng giá trị họ gửi lên.
        String repairDescription = (request.getRepairDescription() != null
                && !request.getRepairDescription().isBlank())
                ? request.getRepairDescription()
                : repairRequest.getIncidentDescription();

        WorkOrder workOrder = workOrderRepository.save(WorkOrder.builder()
                .orderCode(generateOrderCode())
                .repairRequest(repairRequest)
                .leader(leader)
                .directSupervisor(directSupervisor)
                .safetySupervisor(safetySupervisor)
                .startTime(request.getStartTime())
                .repairDescription(repairDescription)
                .status(WorkOrderStatus.STOPPED)
                .createdAt(request.getCreatedAt() != null ? request.getCreatedAt() : LocalDateTime.now())
                .createdBy(createdBy)
                .build());

        List<WorkOrderMember> members = saveMembers(workOrder, request.getMembers());

        // Yêu cầu đã có phiếu công tác => đóng lại, rời khỏi danh sách "chờ xử lý".
        // Đường về nằm ở cancelWorkOrder: huỷ hết PCT thì yêu cầu quay lại PENDING.
        repairRequest.setStatus(RepairRequestStatus.COMPLETED);
        repairRequestRepository.save(repairRequest);

        return WorkOrderDTO.from(workOrder, members);
    }

    @Override
    @Transactional
    public WorkOrderDTO createManualWorkOrder(CreateWorkOrderRequest request, String createdByUsername) {
        List<Integer> ids = request.getEquipmentIds() == null ? List.of()
                : new ArrayList<>(new LinkedHashSet<>(request.getEquipmentIds())); // dedupe, giữ thứ tự chọn
        if (ids.isEmpty()) {
            throw new IllegalStateException("Khong the tao WO thu cong khi khong co thiet bi nao (equipmentIds).");
        }

        List<Equipment> equipments = equipmentRepository.findAllById(ids);
        if (equipments.size() != ids.size()) {
            List<Integer> found = equipments.stream().map(Equipment::getId).toList();
            throw new ObjectNotFoundException("Khong tim thay thiet bi voi id: "
                    + ids.stream().filter(id -> !found.contains(id)).map(String::valueOf)
                            .collect(Collectors.joining(", ")));
        }

        // 1 query cho cả danh sách — phủ CẢ WO thủ công lẫn WO sinh từ RepairRequest.
        List<Object[]> holders = workOrderRepository.findLiveHolders(ids,
                List.of(WorkOrderStatus.OPEN, WorkOrderStatus.IN_PROGRESS,
                        WorkOrderStatus.WAITING_FOR_APPROVAL, WorkOrderStatus.APPROVED,
                        WorkOrderStatus.STOPPED));
        if (!holders.isEmpty()) {
            Map<Integer, String> kksById = equipments.stream()
                    .collect(Collectors.toMap(Equipment::getId, Equipment::getKksCode));
            String detail = holders.stream()
                    .map(row -> kksById.getOrDefault((Integer) row[0], String.valueOf(row[0])) + " -> " + row[1])
                    .collect(Collectors.joining("; "));
            throw new IllegalStateException(
                    "Thiet bi dang nam trong phieu cong tac dang hoat dong (" + detail
                            + "). Hay huy phieu cu truoc khi tao phieu moi.");
        }

        Account createdBy = createdByUsername == null ? null
                : accountRepository.findAccountByUsername(createdByUsername)
                        .orElseThrow(() -> new ObjectNotFoundException(
                                "Khong tim thay tai khoan dang nhap: " + createdByUsername));

        Employee leader = loadEmployee(request.getLeaderId(), "nguoi lanh dao cong viec");
        Employee directSupervisor = loadEmployeeOrNull(request.getDirectSupervisorId(), "chi huy truc tiep");
        Employee safetySupervisor = loadEmployeeOrNull(request.getSafetySupervisorId(), "nguoi giam sat an toan");

        WorkOrder workOrder = WorkOrder.builder()
                .orderCode(generateOrderCode())
                .leader(leader)
                .directSupervisor(directSupervisor)
                .safetySupervisor(safetySupervisor)
                .startTime(request.getStartTime())
                .repairDescription(request.getRepairDescription())
                .status(WorkOrderStatus.OPEN)
                .createdAt(request.getCreatedAt() != null ? request.getCreatedAt() : LocalDateTime.now())
                .createdBy(createdBy)
                .build();

        workOrder.setWorkOrderEquipments(equipments.stream()
                .<WorkOrderEquipment>map(e -> WorkOrderEquipment.builder()
                        .workOrder(workOrder)          // ← thiếu = work_order_id NULL = lỗi NOT NULL
                        .equipment(e)
                        .status(WorkOrderEquipmentStatus.IN_PROGRESS)
                        .build())
                .toList());

        WorkOrder saved = workOrderRepository.save(workOrder);

        List<WorkOrderMember> members = saveMembers(saved, request.getMembers());

        return WorkOrderDTO.from(saved, members);
    }

    @Override
    @Transactional
    public WorkOrderDTO cancelWorkOrder(Integer workOrderId, String username) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Khong tim thay phieu cong tac voi id: " + workOrderId));

        if (workOrder.getStatus() == WorkOrderStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Khong the huy phieu cong tac da hoan thanh (" + workOrder.getOrderCode() + ").");
        }

        // Phiếu đã chạy dù chỉ 1 ngày là đã có công tác thực tế tại hiện trường —
        // phải đóng bằng "hoàn thành", không được xoá dấu vết bằng cách huỷ.
        if (workOrderExtensionRepository.countByWorkOrder_Id(workOrderId) > 0) {
            throw new IllegalStateException(
                    "Khong the huy phieu cong tac (" + workOrder.getOrderCode()
                            + ") da thuc hien it nhat mot ngay cong tac.");
        }

        // Chỉ NGƯỜI TẠO phiếu được huỷ (role đã chặn ở controller). Không miễn trừ
        // cho ADMIN — huỷ phiếu là trách nhiệm của người cấp phiếu.
        Account creator = workOrder.getCreatedBy();
        if (username == null || creator == null || !username.equals(creator.getUsername())) {
            throw new AccessDeniedException(
                    "Chi nguoi tao phieu cong tac (" + workOrder.getOrderCode() + ") moi duoc huy phieu.");
        }

        // Idempotent: đã huỷ rồi thì trả về nguyên trạng, không đụng tới yêu cầu.
        if (workOrder.getStatus() != WorkOrderStatus.CANCELLED) {
            workOrder.setStatus(WorkOrderStatus.CANCELLED);
            workOrderRepository.save(workOrder);

            // PCT thủ công: huỷ phiếu → mọi thiết bị (kể cả đã xong) tự động CANCELED.
            if (workOrder.getRepairRequest() == null) {
                List<WorkOrderEquipment> items = workOrderEquipmentRepository.findByWorkOrder_Id(workOrderId);
                items.forEach(woe -> woe.setStatus(WorkOrderEquipmentStatus.CANCELED));
                workOrderEquipmentRepository.saveAll(items);
            }

            // Không còn phiếu "sống" nào → đưa yêu cầu về PENDING để quay lại hàng chờ.
            // (auto-flush trước SELECT đảm bảo phiếu vừa huỷ đã mang status CANCELLED.)
            RepairRequest repairRequest = workOrder.getRepairRequest();
            if (repairRequest != null && !hasLiveWorkOrder(repairRequest.getId())) {
                repairRequest.setStatus(RepairRequestStatus.PENDING);
                repairRequestRepository.save(repairRequest);
            }

            // Đóng băng bản lưu PDF (best-effort, không bao giờ ném).
            workOrderArchiveService.archiveOnClose(workOrderId);
        }

        return WorkOrderDTO.from(workOrder, workOrder.getMembers());
    }

    private boolean hasLiveWorkOrder(Integer repairRequestId) {
        return workOrderRepository.findByRepairRequest_Id(repairRequestId).stream()
                .anyMatch(MaintenanceService::isLive);
    }

    private List<WorkOrderMember> saveMembers(WorkOrder workOrder, List<CreateWorkOrderRequest.MemberInput> inputs) {
        List<WorkOrderMember> saved = new ArrayList<>();
        if (inputs == null) {
            return saved;
        }
        LocalDateTime now = LocalDateTime.now();
        for (CreateWorkOrderRequest.MemberInput input : inputs) {
            Employee employee = loadEmployee(input.getEmployeeId(), "nhan vien lam viec");
            saved.add(workOrderMemberRepository.save(WorkOrderMember.builder()
                    .workOrder(workOrder)
                    .employees(employee)
                    .joinedAt(now)
                    .build()));
        }
        return saved;
    }

    /**
     * Ràng buộc quan hệ 1 RepairRequest → N WorkOrder khi tạo PCT mới.
     *
     * Chỉ xét các phiếu đang "SỐNG" (OPEN/IN_PROGRESS) của cùng yêu cầu; phiếu
     * CANCELLED (đã huỷ, VD vì kho không cấp được vật tư) và COMPLETED (đã xong)
     * được BỎ QUA — nhờ đó luồng "huỷ phiếu cũ → tạo phiếu mới nội dung tương tự"
     * hoạt động bình thường.
     *
     * Với mỗi phiếu đang sống, phiếu mới bị TỪ CHỐI (409) khi trùng leader,
     * direct supervisor, HOẶC safety supervisor — nhân viên thường (members)
     * được phép trùng, riêng 3 vai trò này thì KHÔNG
     * ({@link DuplicateHumanResourceException}).
     *
     * KHÔNG còn kiểm tra chồng lấn khung giờ: từ V13 phiếu không khai báo mốc
     * kết thúc dự kiến nữa (end_time là giờ kết thúc THỰC TẾ, chỉ có khi phiếu
     * hoàn thành) nên không có khoảng thời gian nào để so.
     */
    private void validateActiveWorkOrderConstraints(RepairRequest repairRequest, CreateWorkOrderRequest input) {
        List<WorkOrder> liveWorkOrders = workOrderRepository.findByRepairRequest_Id(repairRequest.getId())
                .stream()
                .filter(MaintenanceService::isLive)
                .toList();

        for (WorkOrder live : liveWorkOrders) {
            checkDuplicateRole(live, input.getLeaderId(), WorkOrder::getLeader, "Nguoi lanh dao cong viec");
            checkDuplicateRole(live, input.getDirectSupervisorId(), WorkOrder::getDirectSupervisor, "Chi huy truc tiep");
            checkDuplicateRole(live, input.getSafetySupervisorId(), WorkOrder::getSafetySupervisor, "Nguoi giam sat an toan");
        }
    }

    /**
     * Nem {@link DuplicateHumanResourceException} neu {@code inputEmployeeId} (leader /
     * direct supervisor / safety supervisor cua phieu MOI) trung voi nguoi dang giu
     * đúng vai trò đó ở phiếu {@code live} (dang SONG cung yeu cau). Members (nhan vien
     * lam viec thuong) KHONG bi rang buoc nay, chi 3 vai tro quan ly nay moi bi cam trung.
     */
    private void checkDuplicateRole(WorkOrder live, Integer inputEmployeeId,
                                     Function<WorkOrder, Employee> roleGetter, String roleLabel) {
        Employee liveEmployee = roleGetter.apply(live);
        Integer liveEmployeeId = liveEmployee != null ? liveEmployee.getId() : null;
        if (inputEmployeeId != null && Objects.equals(liveEmployeeId, inputEmployeeId)) {
            throw new DuplicateHumanResourceException(
                    roleLabel + " da duoc phan cong o phieu cong tac dang hoat dong (" + live.getOrderCode() + "). "
                            + "Cac phieu hoat dong song song khong duoc trung " + roleLabel + ", hoac hay huy phieu cu (CANCELLED).");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WorkOrderDTO> listWorkOrders(String code, String description,
                                             java.time.LocalDate fromDate, java.time.LocalDate toDate,
                                             Pageable pageable) {
        // Luôn đi qua searchWorkOrders (mọi bộ lọc rỗng = lấy tất cả) để danh
        // sách mặc định cũng được sắp theo tiến độ như khi tìm kiếm.
        String codeKeyword = (code == null || code.isBlank()) ? null : code.trim();
        String descKeyword = (description == null || description.isBlank()) ? null : description.trim();
        Integer searchId = null;
        if (codeKeyword != null) {
            try {
                searchId = Integer.valueOf(codeKeyword);
            } catch (NumberFormatException ignored) {
                // từ khoá không phải số → chỉ tìm theo text
            }
        }
        // toDate là NGÀY bao gồm → cận trên loại trừ = đầu ngày hôm sau.
        LocalDateTime startFrom = fromDate == null ? null : fromDate.atStartOfDay();
        LocalDateTime startTo = toDate == null ? null : toDate.plusDays(1).atStartOfDay();
        Page<WorkOrder> page = workOrderRepository.searchWorkOrders(
                codeKeyword, searchId, descKeyword, startFrom, startTo, pageable);
        return page.map(wo -> {
            List<WorkOrderMember> members = workOrderMemberRepository.findByWorkOrder_Id(wo.getId());
            return WorkOrderDTO.from(wo, members);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public WorkOrderDetailDTO getWorkOrderDetail(Integer workOrderId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Khong tim thay phieu cong tac voi id: " + workOrderId));

        List<WorkOrderMember> members = workOrderMemberRepository.findByWorkOrder_Id(workOrderId);

        return WorkOrderDetailDTO.builder()
                .workOrder(WorkOrderDTO.from(workOrder, members))
                .memberHistory(buildMemberHistory(members))
                .extensions(workOrderExtensionRepository
                        .findByWorkOrder_IdOrderByRequestedAtAsc(workOrderId)
                        .stream().map(WorkOrderExtensionDTO::from).toList())
                .build();
    }

    /**
     * Dựng dòng thời gian ra/vào từ các dòng member: mỗi dòng sinh 1 sự kiện
     * JOINED (joined_at) và, nếu đã rời, 1 sự kiện LEFT (left_at). Sắp xếp TĂNG
     * dần theo thời gian → đọc từ trên xuống đúng thứ tự diễn biến:
     * "A joined 08:00 → B joined 08:00 → A left 12:00 → C joined 13:00 ...".
     * (Nhân viên rời rồi vào lại = dòng member mới → tự có thêm cặp sự kiện.)
     */
    private static List<MemberHistoryEventDTO> buildMemberHistory(List<WorkOrderMember> members) {
        List<MemberHistoryEventDTO> events = new ArrayList<>();
        for (WorkOrderMember m : members) {
            Integer employeeId = m.getEmployees() != null ? m.getEmployees().getId() : null;
            String fullName = m.getEmployees() != null ? m.getEmployees().getFullName() : null;
            if (m.getJoinedAt() != null) {
                events.add(MemberHistoryEventDTO.builder()
                        .employeeId(employeeId).fullName(fullName).role(m.getRoleInTask())
                        .eventType(MemberHistoryEventDTO.EventType.JOINED)
                        .eventTime(m.getJoinedAt())
                        .build());
            }
            if (m.getLeftAt() != null) {
                events.add(MemberHistoryEventDTO.builder()
                        .employeeId(employeeId).fullName(fullName).role(m.getRoleInTask())
                        .eventType(MemberHistoryEventDTO.EventType.LEFT)
                        .eventTime(m.getLeftAt())
                        .build());
            }
        }
        events.sort(Comparator.comparing(MemberHistoryEventDTO::getEventTime,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return events;
    }


    // tìm danh sách các WO không ở trạng thái CANCELLED/COMPLETE
    // => còn lại là những WO có nhân viên bận
    @Override
    @Transactional(readOnly = true)
    public List<Integer> getBusyEmployeeIds(Integer excludeWorkOrderId, List<WorkOrderStatus> statuses) {
        java.util.Set<Integer> busy = new java.util.LinkedHashSet<>();
        // Không truyền statuses → xét mọi phiếu sống (hành vi cũ); truyền vào thì
        // chỉ xét các trạng thái đó (VD IN_PROGRESS cho ô Người giám sát an toàn).
        List<WorkOrderStatus> liveStatuses = (statuses == null || statuses.isEmpty())
                ? List.of(WorkOrderStatus.STOPPED, WorkOrderStatus.IN_PROGRESS)
                : statuses;
        for (Object[] row : workOrderRepository.findRoleHolderEmployeeIds(liveStatuses, excludeWorkOrderId)) {
            for (Object id : row) {
                if (id != null) {
                    busy.add((Integer) id);
                }
            }
        }
        busy.addAll(workOrderMemberRepository.findActiveMemberEmployeeIds(liveStatuses, excludeWorkOrderId));
        return new ArrayList<>(busy);
    }

    @Override
    @Transactional
    public WorkOrderMemberDTO addMember(Integer workOrderId, CreateWorkOrderRequest.MemberInput input) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Khong tim thay phieu cong tac voi id: " + workOrderId));

        if (!isLive(workOrder)) {
            throw new IllegalStateException(
                    "Phieu cong tac (" + workOrder.getOrderCode() + ") da " + workOrder.getStatus()
                            + " — khong the them thanh vien.");
        }

        if (workOrderMemberRepository.existsByWorkOrder_IdAndEmployees_IdAndLeftAtIsNull(
                workOrderId, input.getEmployeeId())) {
            throw new IllegalStateException(
                    "Nhan vien nay dang la thanh vien chua roi cua phieu cong tac ("
                            + workOrder.getOrderCode() + ").");
        }

        Employee employee = loadEmployee(input.getEmployeeId(), "nhan vien lam viec");
        WorkOrderMember member = workOrderMemberRepository.save(WorkOrderMember.builder()
                .workOrder(workOrder)
                .employees(employee)
                .joinedAt(LocalDateTime.now())
                .build());
        return WorkOrderMemberDTO.from(member);
    }

    @Override
    @Transactional
    public WorkOrderMemberDTO leaveMember(Integer workOrderId, Integer memberId) {
        WorkOrderMember member = workOrderMemberRepository.findByIdAndWorkOrder_Id(memberId, workOrderId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Khong tim thay thanh vien id " + memberId + " trong phieu cong tac id " + workOrderId));

        // Idempotent: đã rời rồi thì trả về nguyên trạng (giống cancelWorkOrder).
        if (member.getLeftAt() == null) {
            member.setLeftAt(LocalDateTime.now());
            workOrderMemberRepository.save(member);
        }
        return WorkOrderMemberDTO.from(member);
    }

    @Override
    @Transactional
    public WorkOrderDTO completeWorkOrder(Integer workOrderId) {
        WorkOrder workOrder = loadWorkOrder(workOrderId);

        // Idempotent: đã hoàn thành thì trả về nguyên trạng (giống cancelWorkOrder).
        if (workOrder.getStatus() == WorkOrderStatus.COMPLETED) {
            return WorkOrderDTO.from(workOrder, workOrder.getMembers());
        }
        if (workOrder.getStatus() == WorkOrderStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Khong the hoan thanh phieu cong tac da huy (" + workOrder.getOrderCode() + ").");
        }
        // "Khoá phiếu hoàn thành" bấm ngay khi ngày công tác còn đang mở — đóng nốt
        // dòng ngày đó để nhật ký không bỏ lửng.
        closeOpenWorkDay(workOrderId, null);

        // PCT thủ công: chỉ hoàn thành khi MỌI thiết bị đã làm xong (COMPLETED).
        if (workOrder.getRepairRequest() == null) {
            List<WorkOrderEquipment> pending = workOrderEquipmentRepository
                    .findByWorkOrder_Id(workOrderId).stream()
                    .filter(woe -> woe.getStatus() != WorkOrderEquipmentStatus.COMPLETED)
                    .toList();
            if (!pending.isEmpty()) {
                String names = pending.stream()
                        .map(woe -> woe.getEquipment().getKksCode() + " - " + woe.getEquipment().getName())
                        .collect(Collectors.joining(", "));
                throw new IllegalStateException(
                        "Khong the hoan thanh phieu cong tac (" + workOrder.getOrderCode()
                                + ") vi con thiet bi chua xong: " + names + ".");
            }
        }

        workOrder.setStatus(WorkOrderStatus.COMPLETED);
        // Giờ kết thúc THỰC TẾ của phiếu — chỉ đóng dấu MỘT lần (V13).
        if (workOrder.getEndTime() == null) {
            workOrder.setEndTime(LocalDateTime.now());
        }
        repairHistoryService.createRepairHistory(workOrder);
        // Sau setStatus(COMPLETED) — để phiếu này không tự tính mình là phiếu sống.
        restoreEquipmentIfRepaired(workOrder);
        workOrderRepository.save(workOrder);

        // Đóng băng bản lưu PDF cuối cùng (PCT + phiếu cấp vật tư) — best-effort,
        // không bao giờ làm hỏng việc hoàn thành phiếu.
        workOrderArchiveService.archiveOnClose(workOrderId);

        return WorkOrderDTO.from(workOrder, workOrder.getMembers());
    }

    @Override
    @Transactional
    public WorkOrderDTO closeWorkDay(Integer workOrderId, StopWorkOrderRequest request) {
        WorkOrder workOrder = loadWorkOrder(workOrderId);

        if (workOrder.getStatus() != WorkOrderStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Chi khoa duoc phieu ngay dang mo (IN_PROGRESS) — phieu ("
                            + workOrder.getOrderCode() + ") dang " + workOrder.getStatus() + ".");
        }

        closeOpenWorkDay(workOrderId, request != null ? request.getReason() : null);

        workOrder.setStatus(WorkOrderStatus.STOPPED);
        workOrderRepository.save(workOrder);
        return WorkOrderDTO.from(workOrder, workOrder.getMembers());
    }

    @Override
    @Transactional
    public WorkOrderDTO openWorkDay(Integer workOrderId) {
        WorkOrder workOrder = loadWorkOrder(workOrderId);

        if (workOrder.getStatus() != WorkOrderStatus.STOPPED) {
            throw new IllegalStateException(
                    "Chi mo phieu ngay duoc khi phieu dang tam dung (STOPPED) — phieu ("
                            + workOrder.getOrderCode() + ") dang " + workOrder.getStatus() + ".");
        }

        // Lần mở đầu tiên CHÍNH LÀ bắt đầu phiếu — không có thao tác riêng.
        // Idempotent: nếu còn dòng ngày chưa khoá (khoá hụt hôm trước) thì dùng lại,
        // không đẻ thêm dòng rỗng.
        if (workOrderExtensionRepository
                .findFirstByWorkOrder_IdAndClosedAtIsNullOrderByRequestedAtDesc(workOrderId)
                .isEmpty()) {
            workOrderExtensionRepository.save(WorkOrderExtension.builder()
                    .workOrder(workOrder)
                    .allowedDate(java.time.LocalDate.now())
                    .build());   // requestedAt = giờ mở, tự điền bởi @CreationTimestamp
        }

        workOrder.setStatus(WorkOrderStatus.IN_PROGRESS);
        workOrderRepository.save(workOrder);
        return WorkOrderDTO.from(workOrder, workOrder.getMembers());
    }

    @Override
    @Transactional
    public WorkOrderDTO updateWorkOrder(Integer workOrderId, UpdateWorkOrderRequest request) {
        WorkOrder workOrder = loadWorkOrder(workOrderId);

        // Chỉ chặn phiếu đã kết thúc (COMPLETED/CANCELLED) — bản PDF đã đóng băng
        // làm chứng từ pháp lý, không sửa được nữa. Phiếu đang sống sửa tự do:
        // KHÔNG kiểm tra trùng vai trò / chồng lấn giờ như lúc tạo — hiện trường
        // thay đổi liên tục, Tổ trưởng phải chỉnh được phiếu ngay.
        if (!isLive(workOrder)) {
            throw new IllegalStateException(
                    "Khong sua duoc phieu cong tac da ket thuc (" + workOrder.getOrderCode()
                            + ") — dang " + workOrder.getStatus() + ".");
        }

        // Chỉ ghi đè trường client gửi lên (khác null) — trường bỏ trống giữ nguyên.
        if (request.getLeaderId() != null) {
            workOrder.setLeader(loadEmployee(request.getLeaderId(), "Nguoi lanh dao cong viec"));
        }
        if (request.getDirectSupervisorId() != null) {
            workOrder.setDirectSupervisor(loadEmployee(request.getDirectSupervisorId(), "Chi huy truc tiep"));
        }
        if (request.getSafetySupervisorId() != null) {
            workOrder.setSafetySupervisor(loadEmployee(request.getSafetySupervisorId(), "Nguoi giam sat an toan"));
        }
        if (request.getStartTime() != null) {
            workOrder.setStartTime(request.getStartTime());
        }
        if (request.getRepairDescription() != null && !request.getRepairDescription().isBlank()) {
            workOrder.setRepairDescription(request.getRepairDescription());
        }

        workOrderRepository.save(workOrder);
        return WorkOrderDTO.from(workOrder, workOrder.getMembers());
    }

    // Cập nhật trạng thái WO — không còn vòng phê duyệt nào:
    // 1. Tổ trưởng / Quản đốc SC tạo phiếu -> STOPPED (chờ được mở ra làm).
    // 2. Trưởng ca "mở phiếu ngày" -> ghi 1 dòng nhật ký ngày, status IN_PROGRESS.
    //    Lần mở đầu tiên chính là bắt đầu phiếu.
    // 3. Hết ngày chưa xong: "khoá phiếu ngày" -> đóng dòng nhật ký, quay về STOPPED.
    //    Lặp 2-3 cho tới khi xong.
    // 4. Xong việc: "khoá phiếu hoàn thành" -> COMPLETED, đóng dấu end_time.
    // 5. Huỷ: chỉ khi CHƯA chạy ngày nào và người bấm đúng là người tạo phiếu.

    @Override
    @Transactional
    public WorkOrderDTO updateWorkOrderStatus(Integer workOrderId, UpdateWorkOrderStatusRequest request,
                                              String username) {
        WorkOrder workOrder = loadWorkOrder(workOrderId);
        WorkOrderStatus current = workOrder.getStatus();
        WorkOrderStatus target = request.getTargetStatus();

        // Idempotent: đã ở đúng trạng thái đích thì trả về nguyên trạng.
        if (current == target) {
            return WorkOrderDTO.from(workOrder, workOrder.getMembers());
        }

        // Mọi nhánh uỷ quyền cho method chuyên trách — guard + side effect (nhật ký
        // ngày, đóng băng PDF, trả yêu cầu về hàng chờ) nằm gọn ở đó, không nhân bản.
        return switch (target) {
            case IN_PROGRESS -> openWorkDay(workOrderId);
            case STOPPED -> closeWorkDay(workOrderId, new StopWorkOrderRequest(request.getReason()));
            case COMPLETED -> completeWorkOrder(workOrderId);
            case CANCELLED -> cancelWorkOrder(workOrderId, username);
        };
    }

    @Override
    @Transactional
    public WorkOrderDTO updateWorkOrderEquipmentStatus(Integer workOrderId, Integer equipmentId,
                                                       WorkOrderEquipmentStatus status) {
        WorkOrder workOrder = loadWorkOrder(workOrderId);

        if (workOrder.getRepairRequest() != null) {
            throw new IllegalStateException(
                    "Chi ap dung cho phieu cong tac thu cong (khong co yeu cau sua chua).");
        }
        if (!isLive(workOrder)) {
            // Message PHẢI chứa "ket thuc" — test Step 2 assert hasMessageContaining("ket thuc").
            throw new IllegalStateException(
                    "Phieu cong tac (" + workOrder.getOrderCode() + ") da ket thuc ("
                            + workOrder.getStatus() + ") — khong the cap nhat trang thai thiet bi.");
        }
        if (status == WorkOrderEquipmentStatus.CANCELED) {
            throw new IllegalStateException(
                    "Khong the huy rieng le thiet bi; huy phieu cong tac de huy toan bo thiet bi.");
        }

        WorkOrderEquipment item = workOrderEquipmentRepository
                .findByWorkOrder_IdAndEquipment_Id(workOrderId, equipmentId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Khong tim thay thiet bi id " + equipmentId + " trong phieu cong tac id " + workOrderId));

        item.setStatus(status);
        workOrderEquipmentRepository.save(item);
        return WorkOrderDTO.from(workOrder, workOrder.getMembers());
    }

    /**
     * Đóng dòng nhật ký ngày đang mở (nếu có) — dùng chung cho "khoá phiếu ngày"
     * và "khoá phiếu hoàn thành" nên nhật ký không bao giờ bỏ lửng một ngày.
     * Không có dòng nào đang mở thì không làm gì (dữ liệu cũ trước V20).
     *
     * @param note ghi chú tuỳ chọn, chỉ ghi đè khi khác rỗng.
     */
    private void closeOpenWorkDay(Integer workOrderId, String note) {
        workOrderExtensionRepository
                .findFirstByWorkOrder_IdAndClosedAtIsNullOrderByRequestedAtDesc(workOrderId)
                .ifPresent(day -> {
                    day.setClosedAt(LocalDateTime.now());
                    if (note != null && !note.isBlank()) {
                        day.setReason(note.trim());
                    }
                    workOrderExtensionRepository.save(day);
                });
    }

    /**
     * Trả thiết bị từ Sự cố (FAILURE) về Hoạt động (ACTIVE) khi phiếu vừa khoá là
     * hư hỏng CUỐI CÙNG còn mở của thiết bị đó. Nửa đối xứng của
     * {@code RepairService} — nơi đặt FAILURE lúc tạo yêu cầu sửa chữa.
     *
     * Phải kiểm HAI vế cùng lúc, thiếu vế nào cũng sai:
     *  - Còn yêu cầu PENDING: đã báo hỏng nhưng chưa cấp phiếu công tác.
     *  - Còn phiếu công tác sống: đang sửa dở. Vế này KHÔNG lộ ra ở vế trên vì yêu
     *    cầu chuyển COMPLETED ngay lúc tạo phiếu, chứ không phải lúc sửa xong.
     * Bơm có 2 lỗi, sửa xong 1 lỗi mà báo Hoạt động là nói dối bảng điều khiển.
     *
     * CHỈ động vào thiết bị đang FAILURE. MAINTENANCE / STANDBY / RETIRED là quyết
     * định vận hành khác — khoá một phiếu sửa chữa không đủ tư cách ghi đè chúng.
     *
     * Không gọi save: equipment là managed entity trong @Transactional nên
     * dirty-checking tự flush, giống cách RepairService đặt FAILURE.
     */
    private void restoreEquipmentIfRepaired(WorkOrder workOrder) {
        RepairRequest request = workOrder.getRepairRequest();
        Equipment equipment = request != null ? request.getEquipment() : null;
        if (equipment == null || equipment.getStatus() != EquipmentStatus.FAILURE) {
            return;
        }
        boolean conHuHongKhac =
                repairRequestRepository.existsByEquipment_IdAndStatus(
                        equipment.getId(), RepairRequestStatus.PENDING)
                        || workOrderRepository.existsOtherLiveWorkOrderForEquipment(
                        equipment.getId(), workOrder.getId(),
                        List.of(WorkOrderStatus.STOPPED, WorkOrderStatus.IN_PROGRESS));
        if (!conHuHongKhac) {
            equipment.setStatus(EquipmentStatus.ACTIVE);
        }
    }

    private WorkOrder loadWorkOrder(Integer workOrderId) {
        return workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Khong tim thay phieu cong tac voi id: " + workOrderId));
    }

    /**
     * Phieu "song" = dang rang buoc quan he 1-n (chua huy, chua hoan thanh).
     * STOPPED (tam dung giua 2 ngay cong tac) van la phieu song: van giu nhan su
     * + khung gio, phai chan phieu moi trung tai nguyen.
     */
    private static boolean isLive(WorkOrder wo) {
        return wo.getStatus() == WorkOrderStatus.STOPPED
                || wo.getStatus() == WorkOrderStatus.IN_PROGRESS;
    }

    /**
     * Sinh mã phiếu công tác dạng {@code WO-yyMMddHHmmss-SEQ} (VD
     * "WO-260627153045-003") qua {@link TimeStampCodeGenerator} — KHÔNG còn đọc
     * mã lớn nhất trong DB rồi +1 như trước. Trùng mã (hiếm) được chốt chặn
     * bởi unique constraint ở DB; nếu cần tự sinh lại + thử lại, bọc lời gọi
     * service bằng {@code UniqueCodeRetryExecutor} ở tầng controller.
     */
    private String generateOrderCode() {
        return TimeStampCodeGenerator.generate(WorkOrder.class);
    }

    private Employee loadEmployee(Integer employeeId, String label) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Khong tim thay nhan vien (" + label + ") voi id: " + employeeId));
    }

    private Employee loadEmployeeOrNull(Integer employeeId, String label) {
        return employeeId == null ? null : loadEmployee(employeeId, label);
    }
}