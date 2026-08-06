package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.WorkOrderExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkOrderExtensionRepository extends JpaRepository<WorkOrderExtension, Integer> {

    /**
     * Nhật ký công tác hàng ngày của một phiếu, theo thứ tự MỞ tăng dần — đúng
     * diễn biến thực tế cho cả bảng 5 trên bản PDF lẫn màn hình chi tiết (ngày
     * công tác của dữ liệu cũ có thể null nên không sắp theo nó được).
     */
    List<WorkOrderExtension> findByWorkOrder_IdOrderByRequestedAtAsc(Integer workOrderId);

    /** Ngày công tác ĐANG MỞ (chưa khoá) — nhiều nhất 1 dòng tại một thời điểm. */
    Optional<WorkOrderExtension> findFirstByWorkOrder_IdAndClosedAtIsNullOrderByRequestedAtDesc(
            Integer workOrderId);

    /** Đếm số ngày phiếu đã chạy — 0 là điều kiện cần để huỷ phiếu. */
    long countByWorkOrder_Id(Integer workOrderId);
}
