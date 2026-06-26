package com.example.m6_thermal_power_plant_api.service.soft_delete;

import com.example.m6_thermal_power_plant_api.entity.ConsumableExport;
import com.example.m6_thermal_power_plant_api.entity.ConsumableIssue;
import com.example.m6_thermal_power_plant_api.entity.Equipment;
import com.example.m6_thermal_power_plant_api.entity.SparePartExport;
import com.example.m6_thermal_power_plant_api.entity.SparePartsIssue;
import com.example.m6_thermal_power_plant_api.entity.WorkOrder;
import com.example.m6_thermal_power_plant_api.repository.WorkOrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * "Chốt" hành vi soft-delete CASCADE khi xoá mềm một phiếu công tác (work_order),
 * đặc biệt là nhánh export vừa được gắn {@code @CascadeSoftDelete} ở
 * {@link SparePartExport#getSparePartsIssue()} / {@link ConsumableExport#getConsumableIssue()}.
 *
 * KHÁC với các *DbTest kiểu @Commit (quan sát thủ công), test này dùng @Transactional
 * KHÔNG @Commit nên tự rollback sau khi chạy → lặp lại bao nhiêu lần cũng được,
 * không làm bẩn DB và không cần re-seed.
 *
 * YÊU CẦU: MySQL đang chạy (xem application.properties) và đã nạp sample-data.sql,
 * trong đó work_order id = 1 (WO-2026-0001) còn "sống" cùng các member / issue của nó.
 *
 * Kịch bản:
 *   - Seed thêm 1 spare_part_export + 1 consumable_export trỏ tới issue của WO #1.
 *   - softDelete(WO #1) → kỳ vọng ẩn: chính WO, members, spare/consumable issues,
 *     VÀ 2 export vừa seed (nhánh MỚI). Tất cả cùng một mốc deleted_at.
 *   - KHÔNG ẩn: repair_request cha, catalog spare_part/consumable, equipment
 *     (đều là tham chiếu "đi lên", cascade chỉ đi xuống).
 *   - restore(WO #1) → mọi dòng trên quay lại is_deleted = false.
 */
@SpringBootTest
public class WorkOrderSoftDeleteCascadeTest {

    private static final int WORK_ORDER_ID = 1; // WO-2026-0001 trong sample-data.sql

    @Autowired
    private SoftDeleteCascadeService cascadeService;

    @Autowired
    private WorkOrderRepository workOrderRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    @Transactional // KHÔNG @Commit → rollback tự động, test idempotent
    void deletingWorkOrder_hidesSubtreeIncludingExports_butNotCatalogOrParent() {
        // ---- ARRANGE ---------------------------------------------------------
        WorkOrder wo = workOrderRepository.findById(WORK_ORDER_ID).orElseThrow(() ->
                new RuntimeException("work_order id=" + WORK_ORDER_ID + " không tồn tại hoặc đang bị ẩn. "
                        + "Hãy nạp lại sample-data.sql trước khi chạy test này."));

        SparePartsIssue spi = firstOrThrow(wo.getSparePartsIssues(),
                "WO #" + WORK_ORDER_ID + " không có spare_parts_issue trong sample-data.");
        ConsumableIssue ci = firstOrThrow(wo.getConsumableIssues(),
                "WO #" + WORK_ORDER_ID + " không có consumable_issue trong sample-data.");

        Equipment equipment = wo.getRepairRequest().getEquipment();
        Integer repairRequestId = wo.getRepairRequest().getId();
        Integer sparePartId = spi.getSparePart().getId();
        Integer consumableId = ci.getConsumable().getId();
        Integer equipmentId = equipment.getId();

        // Seed 2 export trỏ tới issue của WO này (sample-data không seed sẵn export).
        SparePartExport spe = SparePartExport.builder()
                .exportCode("TEST-SPE-CASCADE")
                .sparePartsIssue(spi)
                .sparePart(spi.getSparePart())
                .requestedQuantity(new BigDecimal("1.00"))
                .actualQuantity(new BigDecimal("1.00"))
                .equipment(equipment)
                .exportedAt(LocalDateTime.now())
                .build();
        ConsumableExport ce = ConsumableExport.builder()
                .exportCode("TEST-CE-CASCADE")
                .consumableIssue(ci)
                .consumable(ci.getConsumable())
                .requestedQuantity(new BigDecimal("1.00"))
                .actualQuantity(new BigDecimal("1.00"))
                .equipment(equipment)
                .exportedAt(LocalDateTime.now())
                .build();
        em.persist(spe);
        em.persist(ce);
        em.flush();
        Integer speId = spe.getId();
        Integer ceId = ce.getId();

        // Thu thập id con qua native SQL (không bị @SQLRestriction lọc) khi còn "sống".
        List<Integer> memberIds = childIds("work_order_members", "work_order_id", WORK_ORDER_ID);
        List<Integer> spiIds = childIds("spare_parts_issues", "work_order_id", WORK_ORDER_ID);
        List<Integer> ciIds = childIds("consumable_issues", "work_order_id", WORK_ORDER_ID);
        assertThat(memberIds).as("WO #" + WORK_ORDER_ID + " nên có member").isNotEmpty();

        // ---- ACT: xoá mềm cascade --------------------------------------------
        cascadeService.softDelete(wo);

        // ---- ASSERT: nhánh thuộc về WO bị ẩn ---------------------------------
        assertHidden("work_orders", WORK_ORDER_ID);
        memberIds.forEach(id -> assertHidden("work_order_members", id));
        spiIds.forEach(id -> assertHidden("spare_parts_issues", id));
        ciIds.forEach(id -> assertHidden("consumable_issues", id));
        assertHidden("spare_part_exports", speId);   // nhánh MỚI (fix #1)
        assertHidden("consumable_exports", ceId);    // nhánh MỚI (fix #1)

        // ---- ASSERT: tham chiếu "đi lên" KHÔNG bị ẩn -------------------------
        assertVisible("repair_requests", repairRequestId); // cha của WO
        assertVisible("spare_parts", sparePartId);         // catalog
        assertVisible("consumable", consumableId);         // catalog
        assertVisible("equipment", equipmentId);           // thiết bị

        // ---- ASSERT: cùng một lô deleted_at ----------------------------------
        Object woStamp = deletedAt("work_orders", WORK_ORDER_ID);
        assertThat(woStamp).as("work_order phải có deleted_at sau khi xoá mềm").isNotNull();
        assertThat(deletedAt("spare_part_exports", speId)).isEqualTo(woStamp);
        assertThat(deletedAt("consumable_exports", ceId)).isEqualTo(woStamp);

        // ---- ACT: khôi phục cascade ------------------------------------------
        cascadeService.restore(wo);

        // ---- ASSERT: mọi dòng quay lại hiển thị ------------------------------
        assertVisible("work_orders", WORK_ORDER_ID);
        memberIds.forEach(id -> assertVisible("work_order_members", id));
        spiIds.forEach(id -> assertVisible("spare_parts_issues", id));
        ciIds.forEach(id -> assertVisible("consumable_issues", id));
        assertVisible("spare_part_exports", speId);
        assertVisible("consumable_exports", ceId);
    }

    // ---- helpers ------------------------------------------------------------

    private static <T> T firstOrThrow(List<T> list, String message) {
        if (list == null || list.isEmpty()) {
            throw new RuntimeException(message);
        }
        return list.get(0);
    }

    @SuppressWarnings("unchecked")
    private List<Integer> childIds(String table, String fkColumn, int parentId) {
        List<Object> rows = em.createNativeQuery(
                        "select id from " + table + " where " + fkColumn + " = :pid")
                .setParameter("pid", parentId)
                .getResultList();
        return rows.stream().map(o -> ((Number) o).intValue()).toList();
    }

    private void assertHidden(String table, Object id) {
        assertThat(toBool(flag(table, id)))
                .as("%s id=%s phải bị ẩn (is_deleted = true)", table, id)
                .isTrue();
    }

    private void assertVisible(String table, Object id) {
        assertThat(toBool(flag(table, id)))
                .as("%s id=%s phải hiển thị (is_deleted = false)", table, id)
                .isFalse();
    }

    private Object flag(String table, Object id) {
        return em.createNativeQuery("select is_deleted from " + table + " where id = :id")
                .setParameter("id", id)
                .getSingleResult();
    }

    private Object deletedAt(String table, Object id) {
        return em.createNativeQuery("select deleted_at from " + table + " where id = :id")
                .setParameter("id", id)
                .getSingleResult();
    }

    private static boolean toBool(Object v) {
        if (v == null) return false;
        if (v instanceof Boolean b) return b;
        if (v instanceof Number n) return n.intValue() != 0;
        return Boolean.parseBoolean(v.toString());
    }
}
