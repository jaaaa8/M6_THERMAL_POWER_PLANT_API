package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.WorkOrder;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test {@link WorkOrderRepository#searchWorkOrders} trên MySQL thật (rollback
 * sau mỗi test nhờ @Transactional): tìm theo id / repairDescription và thứ tự
 * mặc định theo tiến độ (OPEN → đang làm → chờ duyệt gia hạn → hoàn thành →
 * huỷ; cùng nhóm thì phiếu mới tạo đứng trước).
 */
@SpringBootTest
@Transactional
class WorkOrderRepositorySearchTest {

    @Autowired
    private WorkOrderRepository workOrderRepository;

    private WorkOrder create(String marker, WorkOrderStatus status, LocalDateTime createdAt) {
        return workOrderRepository.save(WorkOrder.builder()
                .orderCode("WOTEST-" + UUID.randomUUID().toString().substring(0, 12))
                .repairDescription(marker + " sua bom cap nuoc")
                .status(status)
                .createdAt(createdAt)
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
                .searchWorkOrders(marker, null, PageRequest.of(0, 20))
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
                String.valueOf(wo.getId()), wo.getId(), PageRequest.of(0, 50));

        assertTrue(page.getContent().stream().anyMatch(w -> w.getId().equals(wo.getId())),
                "Tim theo id phai tra ve dung phieu cong tac");
    }
}
