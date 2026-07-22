package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.WorkOrderExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkOrderExtensionRepository extends JpaRepository<WorkOrderExtension, Integer> {

    /**
     * Lịch sử gia hạn của một phiếu công tác, theo thứ tự GỬI DUYỆT tăng dần —
     * đúng diễn biến thực tế cho cả bảng 5 trên bản PDF lẫn dòng thời gian ở
     * màn hình chi tiết (ngày được duyệt có thể null nên không sắp theo nó được).
     */
    List<WorkOrderExtension> findByWorkOrder_IdOrderByRequestedAtAsc(Integer workOrderId);
}
