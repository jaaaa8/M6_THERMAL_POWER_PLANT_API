package com.example.m6_thermal_power_plant_api.service.maintenance;

import com.example.m6_thermal_power_plant_api.dto.maintenance.CreateWorkOrderRequest;
import com.example.m6_thermal_power_plant_api.dto.maintenance.MemberHistoryEventDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.RepairRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.WorkOrderDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.WorkOrderDetailDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.WorkOrderMemberDTO;
import com.example.m6_thermal_power_plant_api.entity.*;
import com.example.m6_thermal_power_plant_api.entity.enums.RepairRequestStatus;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus;
import com.example.m6_thermal_power_plant_api.exception.DuplicateHumanResourceException;
import com.example.m6_thermal_power_plant_api.exception.ObjectNotFoundException;
import com.example.m6_thermal_power_plant_api.exception.TimeOverlapException;
import com.example.m6_thermal_power_plant_api.repository.*;
import com.example.m6_thermal_power_plant_api.service.spare_part.ISparePartIssuesService;
import com.example.m6_thermal_power_plant_api.util.TimeStampCodeGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final EmployeeRepository employeeRepository;
    private final ISparePartIssuesService sparePartIssuesService;
    private final AccountRepository accountRepository;

    public MaintenanceService(RepairRequestRepository repairRequestRepository,
                              WorkOrderRepository workOrderRepository,
                              WorkOrderMemberRepository workOrderMemberRepository,
                              EmployeeRepository employeeRepository,
                              ISparePartIssuesService sparePartIssuesService,
                              AccountRepository accountRepository) {
        this.repairRequestRepository = repairRequestRepository;
        this.workOrderRepository = workOrderRepository;
        this.workOrderMemberRepository = workOrderMemberRepository;
        this.employeeRepository = employeeRepository;
        this.sparePartIssuesService = sparePartIssuesService;
        this.accountRepository = accountRepository;
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

    /** Phieu "song" = dang rang buoc quan he 1-n (chua huy, chua hoan thanh). */
    private static boolean isLive(WorkOrder wo) {
        return wo.getStatus() == WorkOrderStatus.OPEN || wo.getStatus() == WorkOrderStatus.IN_PROGRESS;
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