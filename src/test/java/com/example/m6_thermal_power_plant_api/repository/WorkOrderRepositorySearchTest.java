package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.Employee;
import com.example.m6_thermal_power_plant_api.entity.WorkOrder;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test {@link WorkOrderRepository#searchWorkOrders} trên MySQL thật (rollback
 * sau mỗi test nhờ @Transactional): 4 bộ lọc AND độc lập — code (id / orderCode
 * / mã nhân viên leader), description (repairDescription), khoảng startTime —
 * và thứ tự mặc định theo tiến độ (OPEN → đang làm → chờ duyệt gia hạn → hoàn
 * thành → huỷ; cùng nhóm thì phiếu mới tạo đứng trước).
 */
@SpringBootTest
@Transactional
class WorkOrderRepositorySearchTest {

    @Autowired
    private WorkOrderRepository workOrderRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private WorkOrder create(String marker, WorkOrderStatus status, LocalDateTime createdAt) {
        return create(marker, status, createdAt, null, null);
    }

    private WorkOrder create(String marker, WorkOrderStatus status, LocalDateTime createdAt,
                             Employee leader, LocalDateTime startTime) {
        return workOrderRepository.save(WorkOrder.builder()
                .orderCode("WOTEST-" + UUID.randomUUID().toString().substring(0, 12))
                .repairDescription(marker + " sua bom cap nuoc")
                .status(status)
                .createdAt(createdAt)
                .leader(leader)
                .startTime(startTime)
                .build());
    }

    private Employee createLeader(String employeeCode) {
        return employeeRepository.save(Employee.builder()
                .employeeCode(employeeCode)
                .fullName("Nhan vien test " + employeeCode)
                .gmail(employeeCode.toLowerCase() + "@test.local")
                .build());
    }

    @Test
    void searchByRepairDescription_sortedByProgressThenNewestFirst() {
        String marker = "wosearch-" + UUID.randomUUID();
        LocalDateTime base = LocalDateTime.now().withNano(0);

        WorkOrder cancelled = create(marker, WorkOrderStatus.CANCELLED, base.minusHours(1));
        WorkOrder completed = create(marker, WorkOrderStatus.COMPLETED, base.minusHours(2));
        WorkOrder waiting = create(marker, WorkOrderStatus.WAITING_FOR_APPROVAL, base.minusHours(3));
        WorkOrder stoppedOld = create(marker, WorkOrderStatus.STOPPED, base.minusHours(5));
        WorkOrder inProgressNew = create(marker, WorkOrderStatus.IN_PROGRESS, base.minusHours(4));
        WorkOrder open = create(marker, WorkOrderStatus.OPEN, base.minusHours(6));

        List<Integer> ids = workOrderRepository
                .searchWorkOrders(null, null, marker, null, null, PageRequest.of(0, 20))
                .map(WorkOrder::getId)
                .getContent();

        // OPEN trước; nhóm đang làm (IN_PROGRESS + STOPPED) trộn chung, mới tạo
        // trước; rồi WAITING_FOR_APPROVAL → COMPLETED → CANCELLED.
        assertEquals(List.of(open.getId(), inProgressNew.getId(), stoppedOld.getId(),
                waiting.getId(), completed.getId(), cancelled.getId()), ids);
    }

    @Test
    void searchById_findsExactWorkOrder() {
        String marker = "wosearch-" + UUID.randomUUID();
        WorkOrder wo = create(marker, WorkOrderStatus.OPEN, LocalDateTime.now().withNano(0));

        Page<WorkOrder> page = workOrderRepository.searchWorkOrders(
                String.valueOf(wo.getId()), wo.getId(), null, null, null, PageRequest.of(0, 50));

        assertTrue(page.getContent().stream().anyMatch(w -> w.getId().equals(wo.getId())),
                "Tim theo id phai tra ve dung phieu cong tac");
    }

    @Test
    void searchByLeaderEmployeeCode_matchesOnlyThatLeader() {
        String marker = "wosearch-" + UUID.randomUUID();
        String leaderCode = "EMTEST-" + UUID.randomUUID().toString().substring(0, 8);
        LocalDateTime base = LocalDateTime.now().withNano(0);

        WorkOrder withLeader = create(marker, WorkOrderStatus.OPEN, base,
                createLeader(leaderCode), null);
        create(marker, WorkOrderStatus.OPEN, base, null, null); // phiếu chưa gán leader

        List<Integer> ids = workOrderRepository
                .searchWorkOrders(leaderCode, null, marker, null, null, PageRequest.of(0, 20))
                .map(WorkOrder::getId)
                .getContent();

        assertEquals(List.of(withLeader.getId()), ids,
                "Tim theo ma nhan vien leader chi tra ve phieu cua leader do");
    }

    @Test
    void searchByOrderCode_keepsWorkOrderWithoutLeader() {
        String marker = "wosearch-" + UUID.randomUUID();
        WorkOrder noLeader = create(marker, WorkOrderStatus.OPEN, LocalDateTime.now().withNano(0));

        Page<WorkOrder> page = workOrderRepository.searchWorkOrders(
                noLeader.getOrderCode(), null, null, null, null, PageRequest.of(0, 50));

        // LEFT JOIN leader: phiếu chưa gán leader vẫn phải tìm được theo orderCode.
        assertTrue(page.getContent().stream().anyMatch(w -> w.getId().equals(noLeader.getId())),
                "Phieu chua gan leader van phai tim duoc theo orderCode");
    }

    @Test
    void dateRange_filtersOnStartTimeInclusiveBothDays() {
        String marker = "wosearch-" + UUID.randomUUID();
        LocalDateTime base = LocalDateTime.now().withNano(0);
        LocalDate day = LocalDate.now().plusYears(1);

        create(marker, WorkOrderStatus.OPEN, base, null, day.minusDays(1).atTime(10, 0));
        WorkOrder inRange = create(marker, WorkOrderStatus.OPEN, base, null, day.atTime(10, 0));
        create(marker, WorkOrderStatus.OPEN, base, null, day.plusDays(1).atTime(10, 0));

        // fromDate = toDate = day → chỉ phiếu bắt đầu TRONG ngày đó (cận trên
        // là đầu ngày hôm sau, loại trừ).
        List<Integer> ids = workOrderRepository
                .searchWorkOrders(null, null, marker, day.atStartOfDay(),
                        day.plusDays(1).atStartOfDay(), PageRequest.of(0, 20))
                .map(WorkOrder::getId)
                .getContent();

        assertEquals(List.of(inRange.getId()), ids);
    }

    @Test
    void codeAndDescriptionCombineWithAnd() {
        String marker = "wosearch-" + UUID.randomUUID();
        WorkOrder wo = create(marker, WorkOrderStatus.OPEN, LocalDateTime.now().withNano(0));

        Page<WorkOrder> page = workOrderRepository.searchWorkOrders(
                wo.getOrderCode(), null, "khong-khop-" + UUID.randomUUID(), null, null,
                PageRequest.of(0, 20));

        assertEquals(0, page.getTotalElements(),
                "Code khop nhung description khong khop → AND phai loai phieu");
    }
}
