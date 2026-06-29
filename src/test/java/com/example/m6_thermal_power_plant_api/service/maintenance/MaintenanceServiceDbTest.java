package com.example.m6_thermal_power_plant_api.service.maintenance;

import com.example.m6_thermal_power_plant_api.dto.maintenance.CreateWorkOrderRequest;
import com.example.m6_thermal_power_plant_api.dto.maintenance.RepairRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.WorkOrderDTO;
import com.example.m6_thermal_power_plant_api.entity.RepairRequest;
import com.example.m6_thermal_power_plant_api.entity.WorkOrder;
import com.example.m6_thermal_power_plant_api.entity.WorkOrderMember;
import com.example.m6_thermal_power_plant_api.entity.enums.RepairRequestStatus;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus;
import com.example.m6_thermal_power_plant_api.repository.RepairRequestRepository;
import com.example.m6_thermal_power_plant_api.repository.WorkOrderMemberRepository;
import com.example.m6_thermal_power_plant_api.repository.WorkOrderRepository;
import lombok.NonNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Kiểm tra thủ công 2 chức năng Sprint 1 (row 43-44) TRỰC TIẾP trên database thật:
 *  - row 43: xem danh sách yêu cầu sửa chữa đang chờ xử lý (status = PENDING).
 *  - row 44: tạo phiếu công tác (PCT) từ 1 yêu cầu; thiết bị lấy từ request,
 *            gắn lãnh đạo / chỉ huy trực tiếp / giám sát an toàn / nhân viên.
 *
 * YÊU CẦU: MySQL đang chạy (xem application.properties) và đã nạp sample-data.sql.
 *
 * CÁCH CHẠY ĐỂ QUAN SÁT:
 *   1. {@link #listPendingRepairRequests_printForManualCheck()} → in ra các request
 *      đang chờ xử lý (sample-data: RR-2026-0002). KHÔNG ghi DB.
 *   2. {@link #createWorkOrderFromPendingRequest_andCommitToDatabase()} → tạo PCT từ
 *      request chờ xử lý đầu tiên rồi @Commit. Mở DB kiểm tra:
 *        - work_orders        : có 1 dòng mới, status = OPEN, order_code = WO-yyMMddHHmmss-NNN
 *        - work_order_members : có thành viên gắn với work_order_id mới
 *        - repair_requests     : status của request đó chuyển PENDING -> IN_PROGRESS
 *
 * LƯU Ý: test (2) KHÔNG idempotent — sau khi chạy, request hết PENDING. Muốn chạy lại
 * thì nạp lại sample-data.sql.
 */
@SpringBootTest
@Tag("manual")  // Chạy thủ công với MySQL thật + sample-data.sql. Không chạy trong CI/CD
public class MaintenanceServiceDbTest {

    @Autowired
    private MaintenanceService maintenanceService;

    @Autowired
    private RepairRequestRepository repairRequestRepository;

    @Autowired
    private WorkOrderRepository workOrderRepository;

    @Autowired
    private WorkOrderMemberRepository workOrderMemberRepository;

    @Test
    void listPendingRepairRequests_printForManualCheck() {
        Pageable pageable = PageRequest.of(0, 50);
        List<RepairRequestDTO> pending = maintenanceService.getPendingRepairRequests(pageable).getContent();

        System.out.println("Pending repair requests (status = PENDING): " + pending.size());
        pending.forEach(r -> System.out.println(
                "  - id=" + r.getId()
                        + " code=" + r.getRequestCode()
                        + " equipment=" + r.getEquipmentKksCode()
                        + " priority=" + r.getPriority()
                        + " status=" + r.getStatus()));

        // Mọi dòng trả về đều phải đúng trạng thái PENDING.
        assertThat(pending).allMatch(r -> r.getStatus() == RepairRequestStatus.PENDING);
    }

    @Test
    @Transactional
    @Commit
    void createWorkOrderFromPendingRequest_andCommitToDatabase() {
        // 1. Lấy 1 yêu cầu đang chờ xử lý (sample-data: RR-2026-0002, chưa có PCT).
        RepairRequestDTO target = maintenanceService.getPendingRepairRequests(PageRequest.of(0, 50)).getContent().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "No PENDING repair request found. Did you run sample-data.sql? "
                                + "(RR-2026-0002 should be PENDING). Re-seed to run this test again."));
        System.out.println("Creating work order from request id=" + target.getId()
                + " (" + target.getRequestCode() + "), equipment=" + target.getEquipmentKksCode());

        // 2. Nhân sự lấy từ sample-data.sql (accounts id 1..6).
        CreateWorkOrderRequest request = getCreateWorkOrderRequest(target);

        WorkOrderDTO created = maintenanceService.createWorkOrderFromRequest(request);

        // 3. Kiểm tra DTO trả về.
        assertThat(created.getId()).isNotNull();
        assertThat(created.getOrderCode()).startsWith("WO-");
        assertThat(created.getStatus()).isEqualTo(WorkOrderStatus.OPEN);
        assertThat(created.getEquipmentKksCode()).isEqualTo(target.getEquipmentKksCode());
        assertThat(created.getMembers()).hasSize(1);

        // 4. Kiểm tra đã ghi xuống DB (đọc lại qua repository).
        WorkOrder saved = workOrderRepository.findById(created.getId())
                .orElseThrow(() -> new RuntimeException("Work order was not persisted!"));
        assertThat(saved.getStatus()).isEqualTo(WorkOrderStatus.OPEN);

        List<WorkOrderMember> savedMembers = workOrderMemberRepository.findByWorkOrder_Id(created.getId());
        assertThat(savedMembers).hasSize(1);

        // 5. Request đã rời khỏi danh sách chờ xử lý (PENDING -> IN_PROGRESS).
        RepairRequest movedRequest = repairRequestRepository.findById(target.getId())
                .orElseThrow(() -> new RuntimeException("Repair request disappeared!"));
        assertThat(movedRequest.getStatus()).isEqualTo(RepairRequestStatus.IN_PROGRESS);
        assertThat(maintenanceService.getPendingRepairRequests(PageRequest.of(0, 50)).getContent())
                .noneMatch(r -> r.getId().equals(target.getId()));

        System.out.println("Done. Created work order " + created.getOrderCode()
                + " (id=" + created.getId() + ") with " + savedMembers.size() + " member(s). "
                + "Check DB: work_orders, work_order_members, and repair_requests.status of id="
                + target.getId() + " should now be IN_PROGRESS.");
    }

    private static @NonNull CreateWorkOrderRequest getCreateWorkOrderRequest(RepairRequestDTO target) {
        CreateWorkOrderRequest request = new CreateWorkOrderRequest();
        request.setRepairRequestId(target.getId());
        request.setLeaderId(2);            // maintenance.leader — người lãnh đạo công việc
        request.setDirectSupervisorId(1);  // shift.leader      — chỉ huy trực tiếp
        request.setSafetySupervisorId(6);  // safety.supervisor — giám sát an toàn

        CreateWorkOrderRequest.MemberInput member = new CreateWorkOrderRequest.MemberInput();
        member.setAccountId(5);            // mechanic.tech
        member.setRoleInTask("Mechanical technician");
        request.setMembers(List.of(member));
        return request;
    }
}
