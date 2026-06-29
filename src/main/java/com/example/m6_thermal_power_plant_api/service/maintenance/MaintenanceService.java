package com.example.m6_thermal_power_plant_api.service.maintenance;

import com.example.m6_thermal_power_plant_api.dto.maintenance.CreateWorkOrderRequest;
import com.example.m6_thermal_power_plant_api.dto.maintenance.RepairRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.WorkOrderDTO;
import com.example.m6_thermal_power_plant_api.entity.*;
import com.example.m6_thermal_power_plant_api.entity.enums.RepairRequestStatus;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus;
import com.example.m6_thermal_power_plant_api.exception.ObjectNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.*;
import com.example.m6_thermal_power_plant_api.util.TimeStampCodeGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class MaintenanceService implements IMaintenanceService {

    private final RepairRequestRepository repairRequestRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderMemberRepository workOrderMemberRepository;
    private final EmployeeRepository employeeRepository;
    private final AccountRepository accountRepository;

    public MaintenanceService(RepairRequestRepository repairRequestRepository,
                              WorkOrderRepository workOrderRepository,
                              WorkOrderMemberRepository workOrderMemberRepository,
                              EmployeeRepository employeeRepository, AccountRepository accountRepository) {
        this.repairRequestRepository = repairRequestRepository;
        this.workOrderRepository = workOrderRepository;
        this.workOrderMemberRepository = workOrderMemberRepository;
        this.employeeRepository = employeeRepository;
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
    public WorkOrderDTO createWorkOrderFromRequest(CreateWorkOrderRequest request) {
        RepairRequest repairRequest = repairRequestRepository.findById(request.getRepairRequestId())
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Khong tim thay yeu cau sua chua voi id: " + request.getRepairRequestId()));

        validateActiveWorkOrderConstraints(repairRequest, request);

        Account leader = loadAccount(request.getLeaderId(), "nguoi lanh dao cong viec");
        Account directSupervisor = loadAccountOrNull(request.getDirectSupervisorId(), "chi huy truc tiep");
        Account safetySupervisor = loadAccountOrNull(request.getSafetySupervisorId(), "nguoi giam sat an toan");

        WorkOrder workOrder = workOrderRepository.save(WorkOrder.builder()
                .orderCode(generateOrderCode())
                .repairRequest(repairRequest)
                .leader(leader)
                .directSupervisor(directSupervisor)
                .safetySupervisor(safetySupervisor)
                .startTime(request.getStartTime())
                .expectedEndTime(request.getExpectedEndTime())
                .status(WorkOrderStatus.OPEN)
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
                    .roleInTask(input.getRoleInTask())
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
     *  (a) cùng Chỉ huy trực tiếp (direct supervisor) — leader / safety supervisor
     *      được phép trùng, riêng direct supervisor thì KHÔNG; HOẶC
     *  (b) khung giờ [startTime, expectedEndTime] CHỒNG LẤN nhau.
     *
     * Hai phiếu song song chỉ hợp lệ khi KHÁC direct supervisor VÀ giờ không đè.
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
            Integer liveDirectId = live.getDirectSupervisor() != null ? live.getDirectSupervisor().getId() : null;
            if (Objects.equals(liveDirectId, input.getDirectSupervisorId())) {
                throw new IllegalStateException(
                        "Da ton tai phieu cong tac dang hoat dong (" + live.getOrderCode() + ") cung Chi huy truc tiep. "
                                + "Cac phieu hoat dong song song phai khac Chi huy truc tiep, hoac hay huy phieu cu (CANCELLED).");
            }
            if (timeOverlaps(input.getStartTime(), input.getExpectedEndTime(), live.getStartTime(), live.getExpectedEndTime())) {
                throw new IllegalStateException(
                        "Thoi gian lam viec chong lan voi phieu cong tac dang hoat dong (" + live.getOrderCode() + "). "
                                + "Hay chon khung gio khac hoac huy phieu cu (CANCELLED).");
            }
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

    private Account loadAccount(Integer accountId, String label) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Khong tim thay tai khoan (" + label + ") voi id: " + accountId));
    }

    private Account loadAccountOrNull(Integer accountId, String label) {
        return accountId == null ? null : loadAccount(accountId, label);
    }
}