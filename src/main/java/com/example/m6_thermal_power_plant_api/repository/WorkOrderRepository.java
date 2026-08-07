package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.WorkOrder;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderType;
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
     * Tìm kiếm phiếu công tác theo BỐN bộ lọc độc lập, kết hợp AND (null = bỏ
     * qua bộ lọc đó, tất cả null = lấy tất cả):
     *  - {@code code}: id phiếu ({@code searchId} — chỉ khi từ khoá là số),
     *    orderCode hoặc MÃ NHÂN VIÊN của người lãnh đạo (leader.employeeCode) —
     *    LEFT JOIN để phiếu chưa gán leader vẫn tìm được theo orderCode.
     *  - {@code description}: mô tả sửa chữa (repairDescription).
     *  - {@code startFrom} / {@code startTo}: khoảng startTime, {@code startTo}
     *    là cận trên KHÔNG bao gồm (service truyền đầu ngày hôm sau).
     * KHÔNG tìm theo requestCode / incidentDescription của yêu cầu. KHÔNG phân
     * biệt hoa/thường.
     *
     * Sắp xếp mặc định theo TIẾN ĐỘ: IN_PROGRESS → STOPPED → COMPLETED/CANCELLED
     * (hai trạng thái này cùng cấp); trong cùng nhóm phiếu mới tạo đứng trước.
     */
    @Query("""
        SELECT wo FROM WorkOrder wo
        LEFT JOIN wo.leader ld
        WHERE (:code IS NULL
               OR wo.id = :searchId
               OR LOWER(wo.orderCode) LIKE LOWER(CONCAT('%', :code, '%'))
               OR LOWER(ld.employeeCode) LIKE LOWER(CONCAT('%', :code, '%')))
          AND (:description IS NULL
               OR LOWER(wo.repairDescription) LIKE LOWER(CONCAT('%', :description, '%')))
          AND (:startFrom IS NULL OR wo.startTime >= :startFrom)
          AND (:startTo IS NULL OR wo.startTime < :startTo)
          AND (:type IS NULL OR wo.type = :type)
        ORDER BY CASE
            WHEN wo.status = com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus.IN_PROGRESS THEN 0
            WHEN wo.status = com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus.STOPPED THEN 1
            WHEN wo.status = com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus.COMPLETED THEN 2
            ELSE 2
        END, wo.createdAt DESC
    """)
    Page<WorkOrder> searchWorkOrders(@Param("code") String code,
                                     @Param("searchId") Integer searchId,
                                     @Param("description") String description,
                                     @Param("startFrom") java.time.LocalDateTime startFrom,
                                     @Param("startTo") java.time.LocalDateTime startTo,
                                     @Param("type") WorkOrderType type,
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

    /**
     * Thiết bị còn PCT nào ĐANG SỐNG ngoài phiếu {@code excludeWorkOrderId} không —
     * dùng khi khoá phiếu hoàn thành để quyết định có trả thiết bị về ACTIVE.
     *
     * Phải loại trừ chính phiếu đang khoá: lúc gọi thì nó đã sang COMPLETED trong
     * bộ nhớ nhưng chưa chắc đã flush, không loại ra thì có thể tự tính mình là
     * phiếu sống và chặn luôn việc phục hồi thiết bị.
     */
    @Query("""
        SELECT COUNT(wo) > 0 FROM WorkOrder wo
        WHERE wo.repairRequest.equipment.id = :equipmentId
          AND wo.id <> :excludeWorkOrderId
          AND wo.status IN :liveStatuses
    """)
    boolean existsOtherLiveWorkOrderForEquipment(
            @Param("equipmentId") Integer equipmentId,
            @Param("excludeWorkOrderId") Integer excludeWorkOrderId,
            @Param("liveStatuses") java.util.Collection<com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus> liveStatuses);

    /** Dashboard: đếm PCT theo trạng thái */
    long countByStatus(com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus status);

    /**
     * WO đang "sống" giữ bất kỳ thiết bị nào trong danh sách — phủ CẢ WO thủ công
     * (wo.workOrderEquipments) lẫn WO sinh từ RepairRequest (wo.repairRequest.equipment).
     * Trả từng dòng [equipmentId, orderCode].
     */
    @Query("SELECT DISTINCT COALESCE(e.id, req.equipment.id), wo.orderCode "
            + "FROM WorkOrder wo "
            + "LEFT JOIN wo.workOrderEquipments woe "
            + "LEFT JOIN woe.equipment e "
            + "LEFT JOIN wo.repairRequest req "
            + "WHERE (e.id IN :equipmentIds OR req.equipment.id IN :equipmentIds) "
            + "AND wo.status IN :statuses "
            + "AND wo.type = :type")
    List<Object[]> findLiveHolders(@Param("equipmentIds") List<Integer> equipmentIds,
                                   @Param("statuses") List<WorkOrderStatus> statuses,
                                   @Param("type") WorkOrderType type);
}