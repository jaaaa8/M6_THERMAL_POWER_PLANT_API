package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.RepairRequest;
import com.example.m6_thermal_power_plant_api.entity.enums.RepairPriority;
import com.example.m6_thermal_power_plant_api.entity.enums.RepairRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepairRequestRepository extends JpaRepository<RepairRequest, Integer> {

    /**
     * Trang yêu cầu sửa chữa theo trạng thái (VD PENDING — đang chờ xử lý).
     * Sắp xếp do {@link Pageable} quyết định (controller mặc định createdAt desc).
     * @SQLRestriction đã tự loại các bản ghi đã xoá mềm.
     */
    Page<RepairRequest> findByStatus(RepairRequestStatus status, Pageable pageable);

    /**
     * Lọc gộp status + priority + từ khoá (mã YC / mã KKS / tên thiết bị), tham số
     * nào null thì bỏ qua điều kiện đó. Cùng pattern với {@code EquipmentSystemRepository.search}.
     * @SQLRestriction tự loại bản ghi đã xoá mềm.
     */
    @Query("SELECT r FROM RepairRequest r LEFT JOIN r.equipment e WHERE "
            + "(:status IS NULL OR r.status = :status) AND "
            + "(:priority IS NULL OR r.priority = :priority) AND "
            + "(:kw IS NULL OR LOWER(r.requestCode) LIKE LOWER(CONCAT('%', :kw, '%')) "
            + "OR LOWER(e.kksCode) LIKE LOWER(CONCAT('%', :kw, '%')) "
            + "OR LOWER(e.name) LIKE LOWER(CONCAT('%', :kw, '%')))")
    Page<RepairRequest> search(@Param("status") RepairRequestStatus status,
                               @Param("priority") RepairPriority priority,
                               @Param("kw") String kw,
                               Pageable pageable);

    long countByStatus(RepairRequestStatus status);

    long countByStatusAndPriority(RepairRequestStatus status, RepairPriority priority);

    // ─── Dashboard Queries ───

    /** KPI: đếm yêu cầu đang active (PENDING + APPROVED + IN_PROGRESS) */
    @Query("SELECT COUNT(r) FROM RepairRequest r WHERE r.status IN (com.example.m6_thermal_power_plant_api.entity.enums.RepairRequestStatus.PENDING, com.example.m6_thermal_power_plant_api.entity.enums.RepairRequestStatus.APPROVED, com.example.m6_thermal_power_plant_api.entity.enums.RepairRequestStatus.IN_PROGRESS)")
    long countActiveRequests();

    /** Area chart: xu hướng sửa chữa N tháng gần nhất */
    @Query(value = """
        SELECT MONTH(r.created_at) AS m,
               COUNT(*) AS total,
               SUM(CASE WHEN r.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed
        FROM repair_requests r
        WHERE r.is_deleted = false
          AND r.created_at >= DATE_SUB(CURDATE(), INTERVAL :months MONTH)
        GROUP BY MONTH(r.created_at)
        ORDER BY MONTH(r.created_at)
        """, nativeQuery = true)
    List<Object[]> getMonthlyTrend(@Param("months") int months);

    /** Bar chart: top N thiết bị sửa nhiều nhất */
    @Query(value = """
        SELECT e.name, COUNT(r.id) AS cnt
        FROM repair_requests r
        JOIN equipment e ON r.equipment_id = e.id
        WHERE r.is_deleted = false
        GROUP BY e.id, e.name
        ORDER BY cnt DESC
        LIMIT :lim
        """, nativeQuery = true)
    List<Object[]> getTopRepairedEquipment(@Param("lim") int lim);

    /** Table: N yêu cầu sửa chữa gần nhất */
    @Query(value = """
        SELECT r.id, r.request_code, e.name AS equipment_name, e.kks_code,
               r.priority, r.status, r.created_at
        FROM repair_requests r
        LEFT JOIN equipment e ON r.equipment_id = e.id
        WHERE r.is_deleted = false
        ORDER BY r.created_at DESC
        LIMIT :lim
        """, nativeQuery = true)
    List<Object[]> findRecentRequests(@Param("lim") int lim);
}
