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
}