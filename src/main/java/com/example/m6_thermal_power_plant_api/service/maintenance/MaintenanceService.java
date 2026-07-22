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
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus;
import com.example.m6_thermal_power_plant_api.exception.DuplicateHumanResourceException;
import com.example.m6_thermal_power_plant_api.exception.ObjectNotFoundException;
import com.example.m6_thermal_power_plant_api.exception.TimeOverlapException;
import com.example.m6_thermal_power_plant_api.repository.*;
import com.example.m6_thermal_power_plant_api.service.leader.repair_history.IRepairHistoryService;
import com.example.m6_thermal_power_plant_api.service.pdf.WorkOrderArchiveService;
import com.example.m6_thermal_power_plant_api.service.spare_part.ISparePartIssuesService;
import com.example.m6_thermal_power_plant_api.util.TimeStampCodeGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Service
public class MaintenanceService implements IMaintenanceService {

    private final RepairRequestRepository repairRequestRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderMemberRepository workOrderMemberRepository;
    private final WorkOrderExtensionRepository workOrderExtensionRepository;
    private final EmployeeRepository employeeRepository;
    private final ISparePartIssuesService sparePartIssuesService;
    private final AccountRepository accountRepository;
    private final WorkOrderArchiveService workOrderArchiveService;
    private final IRepairHistoryService repairHistoryService;
    private final RoleHierarchy roleHierarchy;

    private final com.example.m6_thermal_power_plant_api.repository.equipment.IEquipmentRepository equipmentRepository;

    public MaintenanceService(WorkOrderRepository workOrderRepository,
                              RepairRequestRepository repairRequestRepository,
                              WorkOrderMemberRepository workOrderMemberRepository,
                              WorkOrderExtensionRepository workOrderExtensionRepository,
                              EmployeeRepository employeeRepository,
                              com.example.m6_thermal_power_plant_api.repository.equipment.IEquipmentRepository equipmentRepository,
                              ISparePartIssuesService sparePartIssuesService,
                              AccountRepository accountRepository,
                              WorkOrderArchiveService workOrderArchiveService,IRepairHistoryService repairHistoryService,
                              RoleHierarchy roleHierarchy) {
        this.workOrderRepository = workOrderRepository;
        this.repairRequestRepository = repairRequestRepository;
        this.workOrderMemberRepository = workOrderMemberRepository;
        this.workOrderExtensionRepository = workOrderExtensionRepository;
        this.employeeRepository = employeeRepository;
        this.equipmentRepository = equipmentRepository;
        this.sparePartIssuesService = sparePartIssuesService;
        this.accountRepository = accountRepository;
        this.workOrderArchiveService = workOrderArchiveService;
        this.repairHistoryService = repairHistoryService;
        this.roleHierarchy = roleHierarchy;
    }

    /**
     * MỌI bước chuyển trạng thái PCT (duyệt, bắt đầu, tạm dừng, gửi/duyệt gia
     * hạn, hoàn thành/khoá, huỷ, mở lại) đều thuộc Trưởng ca / Trưởng kíp —
     * user story #37/#38: "TC/TK mở/đóng PCT hằng ngày, khoá phiếu khi đơn vị
     * sửa chữa hoàn thành". Quản đốc SC / Tổ trưởng chỉ quản lý HỒ SƠ phiếu
     * (tạo, sửa thông tin, thành viên, vật tư, PDF), không đổi trạng thái.
     * Dùng ở MỌI nơi dispatch tới các bước này — kể cả khi gọi gián tiếp qua
     * {@code updateWorkOrderStatus} — nên chỉ cần đặt 1 chỗ trong từng method
     * đích, không phải lặp lại ở từng call site.
     */
    private void requireWorkOrderStatusRole() {
        requireAnyRole("ROLE_SHIFT_LEADER", "ROLE_CREW_LEADER");
    }

    /**
     * Kiểm tra role thật của người gọi hiện tại (mở rộng qua {@link RoleHierarchy}
     * nên ADMIN luôn qua — cùng cơ chế với {@code @PreAuthorize hasAnyRole} ở
     * controller, không hardcode ADMIN riêng). Endpoint /status là 1 method gộp
     * nhiều bước chuyển có yêu cầu role khác nhau nên @PreAuthorize ở controller
     * chỉ chặn được thô (ai có 1 trong các role liên quan PCT mới gọi được);
     * kiểm tra tinh ở đây mới quyết định đúng bước chuyển nào ai được làm.
     */
    private void requireAnyRole(String... roles) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var reachable = roleHierarchy.getReachableGrantedAuthorities(authentication.getAuthorities());
        java.util.Set<String> roleSet = java.util.Set.of(roles);
        boolean allowed = reachable.stream().map(GrantedAuthority::getAuthority).anyMatch(roleSet::contains);
        if (!allowed) {
            throw new AccessDeniedException("Ban khong co quyen thuc hien buoc chuyen trang thai nay.");
        }
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
                .expectedEndTime(request.getExpectedEndTime())
                .repairDescription(repairDescription)
                .status(WorkOrderStatus.OPEN)
                .createdAt(request.getCreatedAt() != null ? request.getCreatedAt() : LocalDateTime.now())
                .createdBy(createdBy)
                .build());

        List<WorkOrderMember> members = saveMembers(workOrder, request.getMembers());

        // Yêu cầu đã có phiếu công tác => rời khỏi danh sách "đang chờ xử lý".
//        repairRequest.setStatus(RepairRequestStatus.IN_PROGRESS);
//        repairRequestRepository.save(repairRequest);

        return WorkOrderDTO.from(workOrder, members);
    }

    @Override
    @Transactional
    public WorkOrderDTO cancelWorkOrder(Integer workOrderId) {
        requireWorkOrderStatusRole();
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Khong tim thay phieu cong tac voi id: " + workOrderId));

        if (workOrder.getStatus() == WorkOrderStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Khong the huy phieu cong tac da hoan thanh (" + workOrder.getOrderCode() + ").");
        }

        // Idempotent: đã huỷ rồi thì trả về nguyên trạng, không đụng tới yêu cầu.
        if (workOrder.getStatus() != WorkOrderStatus.CANCELLED) {
            workOrder.setStatus(WorkOrderStatus.CANCELLED);
            workOrderRepository.save(workOrder);

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
     * Với mỗi phiếu đang sống, phiếu mới bị TỪ CHỐI (409) nếu:
     *  (a) trùng leader, direct supervisor, HOẶC safety supervisor — nhân viên
     *      thường (members) được phép trùng, riêng 3 vai trò này thì KHÔNG
     *      ({@link DuplicateHumanResourceException}); HOẶC
     *  (b) khung giờ [startTime, expectedEndTime] CHỒNG LẤN nhau
     *      ({@link TimeOverlapException}).
     *
     * Hai phiếu song song chỉ hợp lệ khi KHÁC cả 3 vai trò trên VÀ giờ không đè.
     *
     * Để kiểm tra (b), khi đã có ít nhất 1 phiếu sống thì phiếu mới BẮT BUỘC khai
     * báo cả startTime lẫn expectedEndTime (nếu thiếu → 409, kèm gợi ý huỷ phiếu cũ).
     */
    private void validateActiveWorkOrderConstraints(RepairRequest repairRequest, CreateWorkOrderRequest input) {
        if (input.getStartTime() != null && input.getExpectedEndTime() != null
                && !input.getExpectedEndTime().isAfter(input.getStartTime())) {
            throw new IllegalArgumentException("expectedEndTime phai sau startTime.");
        }

        List<WorkOrder> liveWorkOrders = workOrderRepository.findByRepairRequest_Id(repairRequest.getId())
                .stream()
                .filter(MaintenanceService::isLive)
                .toList();

        if (liveWorkOrders.isEmpty()) {
            return; // chưa có phiếu sống nào → tạo tự do
        }

        // Đã có phiếu sống → buộc khai báo đủ mốc thời gian để kiểm tra chồng lấn.
        if (input.getStartTime() == null || input.getExpectedEndTime() == null) {
            throw new IllegalStateException(
                    "Yeu cau nay dang co phieu cong tac hoat dong. Phieu moi phai khai bao startTime va "
                            + "expectedEndTime de kiem tra khong trung thoi gian, hoac hay huy (CANCELLED) phieu cu truoc.");
        }

        for (WorkOrder live : liveWorkOrders) {
            checkDuplicateRole(live, input.getLeaderId(), WorkOrder::getLeader, "Nguoi lanh dao cong viec");
            checkDuplicateRole(live, input.getDirectSupervisorId(), WorkOrder::getDirectSupervisor, "Chi huy truc tiep");
            checkDuplicateRole(live, input.getSafetySupervisorId(), WorkOrder::getSafetySupervisor, "Nguoi giam sat an toan");

            if (timeOverlaps(input.getStartTime(), input.getExpectedEndTime(), live.getStartTime(), live.getExpectedEndTime())) {
                throw new TimeOverlapException(
                        "Thoi gian lam viec chong lan voi phieu cong tac dang hoat dong (" + live.getOrderCode() + "). "
                                + "Hay chon khung gio khac hoac huy phieu cu (CANCELLED).");
            }
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
    public Page<WorkOrderDTO> listWorkOrders(String search, Pageable pageable) {
        boolean hasSearch = search != null && !search.isBlank();
        Page<WorkOrder> page = hasSearch
                ? workOrderRepository.searchWorkOrders(search, pageable)
                : workOrderRepository.findAll(pageable);
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
                .sparePartsIssues(sparePartIssuesService.getByWorkOrder(workOrderId))
                .extensions(workOrderExtensionRepository
                        .findByWorkOrder_IdOrderByExtendedUntilAsc(workOrderId)
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

    @Override
    @Transactional(readOnly = true)
    public List<Integer> getBusyEmployeeIds(Integer excludeWorkOrderId) {
        java.util.Set<Integer> busy = new java.util.LinkedHashSet<>();
        List<WorkOrderStatus> liveStatuses = List.of(
                WorkOrderStatus.OPEN, WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.WAITING_FOR_APPROVAL,
                WorkOrderStatus.APPROVED, WorkOrderStatus.STOPPED);
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
        requireWorkOrderStatusRole();
        WorkOrder workOrder = loadWorkOrder(workOrderId);

        // Idempotent: đã hoàn thành thì trả về nguyên trạng (giống cancelWorkOrder).
        if (workOrder.getStatus() == WorkOrderStatus.COMPLETED) {
            return WorkOrderDTO.from(workOrder, workOrder.getMembers());
        }
        if (workOrder.getStatus() == WorkOrderStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Khong the hoan thanh phieu cong tac da huy (" + workOrder.getOrderCode() + ").");
        }
        if (workOrder.getStatus() == WorkOrderStatus.WAITING_FOR_APPROVAL) {
            throw new IllegalStateException(
                    "Phieu cong tac (" + workOrder.getOrderCode() + ") dang cho Truong ca duyet gia han — "
                            + "phai duyet (APPROVED) roi tiep tuc lam viec truoc khi hoan thanh.");
        }

        workOrder.setStatus(WorkOrderStatus.COMPLETED);
        workOrderRepository.save(workOrder);

        // Đóng băng bản lưu PDF cuối cùng (PCT + phiếu cấp vật tư) — best-effort,
        // không bao giờ làm hỏng việc hoàn thành phiếu.
        workOrderArchiveService.archiveOnClose(workOrderId);

        // Cascade trạng thái khi không còn phiếu sống nào cho cùng yêu cầu:
        // - RepairRequest → COMPLETED (yêu cầu sửa chữa đã xử lý xong).
        // - Equipment → ACTIVE (thiết bị đã sửa xong, trở lại hoạt động).
        RepairRequest repairRequest = workOrder.getRepairRequest();
        if (repairRequest != null && !hasLiveWorkOrder(repairRequest.getId())) {
            repairRequest.setStatus(RepairRequestStatus.COMPLETED);
            repairRequestRepository.save(repairRequest);

            Equipment equipment = repairRequest.getEquipment();
            if (equipment != null) {
                equipment.setStatus(EquipmentStatus.ACTIVE);
                equipmentRepository.save(equipment);
            }
        }

        return WorkOrderDTO.from(workOrder, workOrder.getMembers());
    }

    @Override
    @Transactional
    public WorkOrderDTO stopWorkOrder(Integer workOrderId, StopWorkOrderRequest request) {
        requireWorkOrderStatusRole();
        WorkOrder workOrder = loadWorkOrder(workOrderId);

        // Môi trường làm việc thay đổi liên tục → cho gửi duyệt từ MỌI trạng thái
        // đang sống (OPEN/IN_PROGRESS/APPROVED). Chỉ chặn phiếu đã kết thúc và
        // phiếu ĐANG chờ duyệt (đã có dòng gia hạn treo, duyệt xong mới gửi tiếp).
        if (!isLive(workOrder) || workOrder.getStatus() == WorkOrderStatus.WAITING_FOR_APPROVAL) {
            throw new IllegalStateException(
                    "Khong gui duyet duoc phieu cong tac (" + workOrder.getOrderCode()
                            + ") dang " + workOrder.getStatus() + ".");
        }

        // Dòng gia hạn CHƯA có người duyệt = bằng chứng "đang chờ Trưởng ca ký bản
        // giấy" — được in vào mục "Cho phép làm việc và kết thúc công tác hàng ngày"
        // của bản PDF để đưa tay cho Trưởng ca.
        workOrderExtensionRepository.save(WorkOrderExtension.builder()
                .workOrder(workOrder)
                .reason(request.getReason())
                .extendedUntil(request.getExtendedUntil())
                .build());

        workOrder.setStatus(WorkOrderStatus.WAITING_FOR_APPROVAL);
        workOrderRepository.save(workOrder);
        return WorkOrderDTO.from(workOrder, workOrder.getMembers());
    }

    @Override
    @Transactional
    public WorkOrderDTO approveExtension(Integer workOrderId, String approvedByUsername) {
        requireWorkOrderStatusRole();
        WorkOrder workOrder = loadWorkOrder(workOrderId);

        if (workOrder.getStatus() != WorkOrderStatus.WAITING_FOR_APPROVAL) {
            throw new IllegalStateException(
                    "Phieu cong tac (" + workOrder.getOrderCode() + ") khong o trang thai cho duyet "
                            + "(WAITING_FOR_APPROVAL) — dang " + workOrder.getStatus() + ".");
        }

        Account approvedBy = accountRepository.findAccountByUsername(approvedByUsername)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Khong tim thay tai khoan dang nhap: " + approvedByUsername));

        // Dòng chờ duyệt = dòng MỚI NHẤT chưa có approvedBy. Người bấm xác nhận
        // online chịu trách nhiệm nhập đúng theo bản giấy Trưởng ca đã ký.
        WorkOrderExtension pending = workOrderExtensionRepository
                .findByWorkOrder_IdOrderByExtendedUntilAsc(workOrderId).stream()
                .filter(e -> e.getApprovedBy() == null)
                .reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalStateException(
                        "Phieu cong tac (" + workOrder.getOrderCode()
                                + ") khong co dong gia han nao dang cho duyet."));

        pending.setApprovedBy(approvedBy);
        workOrderExtensionRepository.save(pending);

        workOrder.setStatus(WorkOrderStatus.APPROVED);
        workOrderRepository.save(workOrder);
        return WorkOrderDTO.from(workOrder, workOrder.getMembers());
    }

    @Override
    @Transactional
    public WorkOrderDTO reopenWorkOrder(Integer workOrderId) {
        requireWorkOrderStatusRole();
        WorkOrder workOrder = loadWorkOrder(workOrderId);

        if (workOrder.getStatus() != WorkOrderStatus.OPEN
                && workOrder.getStatus() != WorkOrderStatus.APPROVED) {
            throw new IllegalStateException(
                    "Chi mo lam viec duoc phieu moi tao (OPEN) hoac da duyet gia han (APPROVED) — phieu ("
                            + workOrder.getOrderCode() + ") dang " + workOrder.getStatus() + ".");
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
        if (request.getExpectedEndTime() != null) {
            workOrder.setExpectedEndTime(request.getExpectedEndTime());
        }
        if (request.getRepairDescription() != null && !request.getRepairDescription().isBlank()) {
            workOrder.setRepairDescription(request.getRepairDescription());
        }

        workOrderRepository.save(workOrder);
        return WorkOrderDTO.from(workOrder, workOrder.getMembers());
    }

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

        switch (target) {
            case APPROVED -> {
                // 2 luồng duyệt: phiếu mới (OPEN — duyệt phiếu, không cần dòng gia
                // hạn) và duyệt gia hạn (WAITING_FOR_APPROVAL — gắn approvedBy vào
                // dòng gia hạn đang chờ, tái dùng logic sẵn có).
                if (current == WorkOrderStatus.WAITING_FOR_APPROVAL) {
                    return approveExtension(workOrderId, username);
                }
                if (current != WorkOrderStatus.OPEN) {
                    throw new IllegalStateException(
                            "Chi duyet duoc phieu dang cho duyet (OPEN / WAITING_FOR_APPROVAL) — phieu ("
                                    + workOrder.getOrderCode() + ") dang " + current + ".");
                }
                requireWorkOrderStatusRole();
                workOrder.setStatus(WorkOrderStatus.APPROVED);
            }
            case IN_PROGRESS -> {
                // Bắt buộc duyệt trước khi làm việc (quyết định 2026-07-08).
                if (current != WorkOrderStatus.APPROVED) {
                    throw new IllegalStateException(
                            "Phieu (" + workOrder.getOrderCode() + ") phai duoc duyet (APPROVED) truoc khi "
                                    + "bat dau lam viec — dang " + current + ".");
                }
                requireWorkOrderStatusRole();
                workOrder.setStatus(WorkOrderStatus.IN_PROGRESS);
            }
            case STOPPED -> {
                if (current != WorkOrderStatus.IN_PROGRESS) {
                    throw new IllegalStateException(
                            "Chi tam dung duoc phieu dang thuc hien (IN_PROGRESS) — phieu ("
                                    + workOrder.getOrderCode() + ") dang " + current + ".");
                }
                requireWorkOrderStatusRole();
                workOrder.setStatus(WorkOrderStatus.STOPPED);
            }
            case WAITING_FOR_APPROVAL -> {
                // Gửi duyệt gia hạn: cần lý do + ngày (in lên bản giấy) — tái dùng
                // stopWorkOrder để tạo dòng gia hạn.
                if (request.getReason() == null || request.getReason().isBlank()
                        || request.getExtendedUntil() == null) {
                    throw new IllegalArgumentException(
                            "Gui duyet gia han can ly do (reason) va ngay xin phep (extendedUntil).");
                }
                return stopWorkOrder(workOrderId,
                        new StopWorkOrderRequest(request.getReason().trim(), request.getExtendedUntil()));
            }
            case COMPLETED -> {
                return completeWorkOrder(workOrderId); // giữ nguyên guard + đóng băng PDF
            }
            case CANCELLED -> {
                return cancelWorkOrder(workOrderId); // giữ nguyên side effect (trả yêu cầu về hàng chờ, archive)
            }
            case OPEN -> throw new IllegalStateException(
                    "Khong the dua phieu (" + workOrder.getOrderCode() + ") ve trang thai moi tao (OPEN).");
        }

        workOrderRepository.save(workOrder);
        return WorkOrderDTO.from(workOrder, workOrder.getMembers());
    }

    private WorkOrder loadWorkOrder(Integer workOrderId) {
        return workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Khong tim thay phieu cong tac voi id: " + workOrderId));
    }

    /**
     * Phieu "song" = dang rang buoc quan he 1-n (chua huy, chua hoan thanh).
     * WAITING_FOR_APPROVAL / APPROVED / STOPPED (tam dung, cho lam tiep) van la
     * phieu song: van giu nhan su + khung gio, phai chan phieu moi trung tai nguyen.
     */
    private static boolean isLive(WorkOrder wo) {
        return wo.getStatus() == WorkOrderStatus.OPEN
                || wo.getStatus() == WorkOrderStatus.IN_PROGRESS
                || wo.getStatus() == WorkOrderStatus.WAITING_FOR_APPROVAL
                || wo.getStatus() == WorkOrderStatus.APPROVED
                || wo.getStatus() == WorkOrderStatus.STOPPED;
    }

    /**
     * Hai khoảng [s1,e1) và [s2,e2) chồng lấn khi {@code s1 < e2 && s2 < e1}
     * (chạm đúng điểm cuối — e1 == s2 — KHÔNG tính là trùng). Thiếu bất kỳ mốc
     * nào (null) thì coi như không khẳng định được chồng lấn → trả false.
     */
    private static boolean timeOverlaps(LocalDateTime s1, LocalDateTime e1, LocalDateTime s2, LocalDateTime e2) {
        if (s1 == null || e1 == null || s2 == null || e2 == null) {
            return false;
        }
        return s1.isBefore(e2) && s2.isBefore(e1);
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