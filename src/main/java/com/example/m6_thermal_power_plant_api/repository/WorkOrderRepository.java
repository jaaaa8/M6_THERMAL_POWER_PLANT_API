package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.WorkOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Integer> {

    /** Lấy danh sách phiếu công tác theo ID yêu cầu sửa chữa */
    List<WorkOrder> findByRepairRequest_Id(Integer repairRequestId);

    /**
     * Tìm kiếm phiếu công tác theo từ khoá — CHỈ trên cột của chính phiếu
     * (user-specified): id phiếu ({@code searchId} — chỉ khi từ khoá là số),
     * orderCode, repairDescription. KHÔNG tìm theo requestCode /
     * incidentDescription của yêu cầu. KHÔNG phân biệt hoa/thường.
     * Từ khoá null/rỗng = lấy tất cả.
     *
     * Sắp xếp mặc định theo TIẾN ĐỘ: OPEN → đang làm (APPROVED / IN_PROGRESS /
     * STOPPED) → WAITING_FOR_APPROVAL → COMPLETED → CANCELLED; trong cùng nhóm
     * phiếu mới tạo đứng trước.
     */
    @Query("""
        SELECT wo FROM WorkOrder wo
        WHERE (:search IS NULL OR :search = '')
           OR wo.id = :searchId
           OR LOWER(wo.orderCode) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(wo.repairDescription) LIKE LOWER(CONCAT('%', :search, '%'))
        ORDER BY CASE
            WHEN wo.status = com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus.OPEN THEN 0
            WHEN wo.status = com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus.APPROVED THEN 1
            WHEN wo.status = com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus.IN_PROGRESS THEN 1
            WHEN wo.status = com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus.STOPPED THEN 1
            WHEN wo.status = com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus.WAITING_FOR_APPROVAL THEN 2
            WHEN wo.status = com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus.COMPLETED THEN 3
            ELSE 4
        END, wo.createdAt DESC
    """)
    Page<WorkOrder> searchWorkOrders(@Param("search") String search,
                                     @Param("searchId") Integer searchId,
                                     Pageable pageable);

    /**
     * Bộ ba nhân sự phụ trách (leader / chỉ huy trực tiếp / giám sát an toàn) của
     * mọi phiếu công tác có status thuộc {@code statuses} (trừ phiếu
     * {@code excludeId} nếu truyền). Mỗi phần tử là Object[3] id nhân viên, phần
     * tử có thể null — dùng cho bộ lọc "nhân viên đang bận" khi thêm nhân sự.
     */
    @Query("""
        SELECT l.id, d.id, s.id FROM WorkOrder wo
        LEFT JOIN wo.leader l
        LEFT JOIN wo.directSupervisor d
        LEFT JOIN wo.safetySupervisor s
        WHERE wo.status IN :statuses
          AND (:excludeId IS NULL OR wo.id <> :excludeId)
    """)
    List<Object[]> findRoleHolderEmployeeIds(
            @Param("statuses") java.util.Collection<com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus> statuses,
            @Param("excludeId") Integer excludeId);
}