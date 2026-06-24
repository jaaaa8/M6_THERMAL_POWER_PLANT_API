package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Integer> {

    /** Mỗi yêu cầu sửa chữa chỉ sinh ra đúng 1 phiếu công tác (quan hệ 1-1). */
    boolean existsByRepairRequest_Id(Integer repairRequestId);

    /**
     * Lấy phiếu công tác có mã lớn nhất theo tiền tố năm (VD prefix = "WO-2026-").
     * Vì mã được zero-pad 4 chữ số nên thứ tự chữ cái giảm dần = thứ tự số giảm dần,
     * dùng để sinh số thứ tự tiếp theo cho orderCode.
     */
    Optional<WorkOrder> findFirstByOrderCodeStartingWithOrderByOrderCodeDesc(String prefix);
}
