package com.example.m6_thermal_power_plant_api.service.maintenance;

import com.example.m6_thermal_power_plant_api.dto.maintenance.CreateWorkOrderRequest;
import com.example.m6_thermal_power_plant_api.dto.maintenance.RepairRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.WorkOrderDTO;
import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.RepairRequest;
import com.example.m6_thermal_power_plant_api.entity.WorkOrder;
import com.example.m6_thermal_power_plant_api.entity.WorkOrderMember;
import com.example.m6_thermal_power_plant_api.entity.enums.RepairRequestStatus;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus;
import com.example.m6_thermal_power_plant_api.exception.ObjectNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.AccountRepository;
import com.example.m6_thermal_power_plant_api.repository.RepairRequestRepository;
import com.example.m6_thermal_power_plant_api.repository.WorkOrderMemberRepository;
import com.example.m6_thermal_power_plant_api.repository.WorkOrderRepository;
import com.example.m6_thermal_power_plant_api.service.IMaintenanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MaintenanceService implements IMaintenanceService {

    private static final String ORDER_CODE_PREFIX = "WO-";

    private final RepairRequestRepository repairRequestRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderMemberRepository workOrderMemberRepository;
    private final AccountRepository accountRepository;

    public MaintenanceService(RepairRequestRepository repairRequestRepository,
                              WorkOrderRepository workOrderRepository,
                              WorkOrderMemberRepository workOrderMemberRepository,
                              AccountRepository accountRepository) {
        this.repairRequestRepository = repairRequestRepository;
        this.workOrderRepository = workOrderRepository;
        this.workOrderMemberRepository = workOrderMemberRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepairRequestDTO> getPendingRepairRequests() {
        return repairRequestRepository
                .findByStatusOrderByCreatedAtDesc(RepairRequestStatus.PENDING)
                .stream()
                .map(RepairRequestDTO::from)
                .toList();
    }

    @Override
    @Transactional
    public WorkOrderDTO createWorkOrderFromRequest(CreateWorkOrderRequest request) {
        RepairRequest repairRequest = repairRequestRepository.findById(request.getRepairRequestId())
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Khong tim thay yeu cau sua chua voi id: " + request.getRepairRequestId()));

        List<WorkOrder> existingWorkOrders = workOrderRepository.findByRepairRequest_Id(repairRequest.getId());
        for (WorkOrder oldWo : existingWorkOrders) {
            if (request.getStartTime() != null && oldWo.getStartTime() != null && oldWo.getEndTime() != null) {
                if (!request.getStartTime().isBefore(oldWo.getStartTime()) && !request.getStartTime().isAfter(oldWo.getEndTime())) {
                    throw new IllegalStateException("Thoi gian bat dau trung voi phieu cong tac khac cua yeu cau nay.");
                }
            }

            boolean sameLeader = java.util.Objects.equals(request.getLeaderId(), oldWo.getLeader() != null ? oldWo.getLeader().getId() : null);
            boolean sameDirect = java.util.Objects.equals(request.getDirectSupervisorId(), oldWo.getDirectSupervisor() != null ? oldWo.getDirectSupervisor().getId() : null);
            boolean sameSafety = java.util.Objects.equals(request.getSafetySupervisorId(), oldWo.getSafetySupervisor() != null ? oldWo.getSafetySupervisor().getId() : null);

            if (sameLeader && sameDirect && sameSafety) {
                List<Integer> oldMemberIds = oldWo.getMembers() == null ? java.util.Collections.emptyList() :
                        oldWo.getMembers().stream()
                                .map(m -> m.getAccount().getId())
                                .sorted()
                                .toList();
                List<Integer> newMemberIds = request.getMembers() == null ? java.util.Collections.emptyList() :
                        request.getMembers().stream()
                                .map(CreateWorkOrderRequest.MemberInput::getAccountId)
                                .sorted()
                                .toList();

                if (oldMemberIds.equals(newMemberIds)) {
                    throw new IllegalStateException("Khong the tao phieu cong tac moi voi cung mot doi ngu (Leader, Direct Supervisor, Safety Supervisor, va Members).");
                }
            }
        }

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
                .status(WorkOrderStatus.OPEN)
                .build());

        List<WorkOrderMember> members = saveMembers(workOrder, request.getMembers());

        // Yêu cầu đã có phiếu công tác => rời khỏi danh sách "đang chờ xử lý".
        repairRequest.setStatus(RepairRequestStatus.IN_PROGRESS);
        repairRequestRepository.save(repairRequest);

        return WorkOrderDTO.from(workOrder, members);
    }

    private List<WorkOrderMember> saveMembers(WorkOrder workOrder, List<CreateWorkOrderRequest.MemberInput> inputs) {
        List<WorkOrderMember> saved = new ArrayList<>();
        if (inputs == null) {
            return saved;
        }
        LocalDateTime now = LocalDateTime.now();
        for (CreateWorkOrderRequest.MemberInput input : inputs) {
            Account account = loadAccount(input.getAccountId(), "nhan vien lam viec");
            saved.add(workOrderMemberRepository.save(WorkOrderMember.builder()
                    .workOrder(workOrder)
                    .account(account)
                    .roleInTask(input.getRoleInTask())
                    .joinedAt(now)
                    .build()));
        }
        return saved;
    }

    /**
     * Sinh mã phiếu công tác dạng WO-{năm}-{4 chữ số} dựa trên mã lớn nhất hiện có
     * trong năm (VD WO-2026-0003 -> WO-2026-0004).
     */
    private String generateOrderCode() {
        String prefix = ORDER_CODE_PREFIX + LocalDate.now().getYear() + "-";
        int next = workOrderRepository.findFirstByOrderCodeStartingWithOrderByOrderCodeDesc(prefix)
                .map(wo -> parseSequence(wo.getOrderCode(), prefix) + 1)
                .orElse(1);
        return prefix + String.format("%04d", next);
    }

    private int parseSequence(String orderCode, String prefix) {
        try {
            return Integer.parseInt(orderCode.substring(prefix.length()));
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return 0;
        }
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
