package com.example.m6_thermal_power_plant_api.service.maintenance;

import com.example.m6_thermal_power_plant_api.dto.maintenance.CreateRepairRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.RepairRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IRepairService {

    /**
     * User Story #39 (row 43): xem danh sách các yêu cầu sửa chữa đang chờ xử lý
     * (status = PENDING), CÓ PHÂN TRANG. Thứ tự sắp xếp lấy từ {@code pageable}
     * (controller mặc định createdAt giảm dần — mới nhất lên trước).
     */
    Page<RepairRequestDTO> getPendingRepairRequests(Pageable pageable);

    /**
     * Lấy danh sách TẤT CẢ các yêu cầu sửa chữa, có thể lọc theo trạng thái nếu cần.
     * CÓ PHÂN TRANG.
     */
    Page<RepairRequestDTO> getAllRepairRequests(com.example.m6_thermal_power_plant_api.entity.enums.RepairRequestStatus status, Pageable pageable);

    /**
     * Tạo mới một yêu cầu sửa chữa.
     */
    RepairRequestDTO createRepairRequest(CreateRepairRequestDTO dto, String requesterUsername);

    /**
     * Xóa một yêu cầu sửa chữa. Chỉ cho phép xóa khi yêu cầu chưa từng được tạo PCT (workOrders rỗng).
     */
    void deleteRepairRequest(Integer id, String requesterUsername);
}
