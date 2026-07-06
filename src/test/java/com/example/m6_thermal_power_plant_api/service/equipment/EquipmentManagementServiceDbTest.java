package com.example.m6_thermal_power_plant_api.service.equipment;

import com.example.m6_thermal_power_plant_api.entity.Equipment;
import com.example.m6_thermal_power_plant_api.repository.equipment.IEquipmentRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Kiểm tra thủ công cơ chế soft-delete / restore CASCADE trực tiếp trên database thật.
 *
 * Cả hai test đều thao tác trên "thiết bị mới nhất" (id lớn nhất trong bảng equipment,
 * kể cả bản ghi đã bị xoá mềm) — tức là dòng equipment mới nhất do Flyway nạp qua
 * V3__seed_sample_data.sql, cùng toàn bộ dữ liệu liên quan (equipment_parameters,
 * repair_requests, work_orders, lubrication_plans, ...).
 *
 * CÁCH CHẠY ĐỂ QUAN SÁT:
 *   1. Chạy {@link #deleteLatestEquipment_andCommitToDatabase()} → mở DB kiểm tra:
 *      thiết bị mới nhất và các dòng tham chiếu nó đều có is_deleted = 1 (bị ẩn).
 *   2. Chạy {@link #restoreLatestEquipment_andCommitToDatabase()} → mở DB kiểm tra:
 *      tất cả các dòng đó quay lại is_deleted = 0 (hiện lại).
 *
 * Vì dùng @Commit nên thay đổi được ghi thật xuống DB (không rollback sau test).
 */
@SpringBootTest
@Tag("manual")  // Chạy thủ công với DB thật (Flyway đã nạp sample data). Không chạy trong CI/CD
public class EquipmentManagementServiceDbTest {

    @Autowired
    private EquipmentManagementService equipmentManagementService;

    @Autowired
    private IEquipmentRepository equipmentRepository;

    @Test
    @Transactional
    @Commit
    void deleteLatestEquipment_andCommitToDatabase() {
        // 1. Lấy thiết bị mới nhất (id lớn nhất), kể cả khi đã bị xoá mềm.
        Equipment latest = equipmentRepository.findLatestIncludingDeleted()
                .orElseThrow(() -> new RuntimeException("No equipment found. Drop the schema and restart the app so Flyway re-seeds it."));
        Integer latestId = latest.getId();
        System.out.println("Soft-deleting latest equipment id = " + latestId + " (" + latest.getName() + ")");

        // 2. Xoá mềm cascade (ẩn thiết bị + mọi dependent tham chiếu nó).
        equipmentManagementService.deleteEquipment(latestId);

        // 3. Bản ghi vẫn còn trong DB nhưng đã bị đánh dấu is_deleted = true.
        Equipment deleted = equipmentRepository.findByIdIncludingDeleted(latestId)
                .orElseThrow(() -> new RuntimeException("Equipment was hard-deleted, expected soft-delete!"));
        assertThat(deleted.getIsDeleted()).isTrue();

        System.out.println("Done. Check the database: equipment id " + latestId
                + " and its related rows should now have is_deleted = 1 (hidden).");
    }

    @Test
    @Transactional
    @Commit
    void restoreLatestEquipment_andCommitToDatabase() {
        // 1. Lấy lại đúng thiết bị mới nhất (kể cả khi đang bị ẩn vì is_deleted = true).
        Equipment latest = equipmentRepository.findLatestIncludingDeleted()
                .orElseThrow(() -> new RuntimeException("No equipment found. Drop the schema and restart the app so Flyway re-seeds it."));
        Integer latestId = latest.getId();
        System.out.println("Restoring latest equipment id = " + latestId + " (" + latest.getName() + ")");

        // 2. Khôi phục cascade (hiện lại thiết bị + mọi dependent đã bị ẩn theo nó).
        equipmentManagementService.restoreEquipment(latestId);

        // 3. Thiết bị quay lại trạng thái is_deleted = false.
        Equipment restored = equipmentRepository.findByIdIncludingDeleted(latestId)
                .orElseThrow(() -> new RuntimeException("Equipment disappeared, restore failed!"));
        assertThat(restored.getIsDeleted()).isFalse();

        System.out.println("Done. Check the database: equipment id " + latestId
                + " and its related rows should now have is_deleted = 0 (visible again).");
    }
}
