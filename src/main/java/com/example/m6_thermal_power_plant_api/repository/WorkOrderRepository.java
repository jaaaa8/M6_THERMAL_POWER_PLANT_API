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
     * Tìm kiếm phiếu công tác theo từ khoá: orderCode, requestCode hoặc
     * incidentDescription (thông qua repairRequest). KHÔNG phân biệt hoa/thường.
     */
    @Query("""
        SELECT wo FROM WorkOrder wo
        LEFT JOIN wo.repairRequest rr
        WHERE (:search IS NULL OR :search = '')
           OR LOWER(wo.orderCode) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(rr.requestCode) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(rr.incidentDescription) LIKE LOWER(CONCAT('%', :search, '%'))
    """)
    Page<WorkOrder> searchWorkOrders(@Param("search") String search, Pageable pageable);

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

    /** Dashboard: đếm PCT theo trạng thái */
    long countByStatus(com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus status);
}