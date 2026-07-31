package com.example.m6_thermal_power_plant_api.service.maintenance;

import com.example.m6_thermal_power_plant_api.dto.maintenance.CreateRepairRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.RepairRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.RepairRequestStatsDTO;
import com.example.m6_thermal_power_plant_api.entity.enums.RepairPriority;
import com.example.m6_thermal_power_plant_api.entity.enums.RepairRequestStatus;
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
     * Lấy danh sách yêu cầu sửa chữa, lọc gộp theo trạng thái + độ ưu tiên + từ khoá
     * (mã YC / mã KKS / tên thiết bị) — tham số nào null thì bỏ qua. CÓ PHÂN TRANG.
     */
    Page<RepairRequestDTO> getAllRepairRequests(RepairRequestStatus status,
                                                RepairPriority priority,
                                                String search,
                                                Pageable pageable);

    /**
     * Số liệu tổng hợp (đếm trên toàn bộ, không phụ thuộc trang) cho stat cards + pill counts.
     */
    RepairRequestStatsDTO getStats();

    /**
     * Tạo mới một yêu cầu sửa chữa.
     */
    RepairRequestDTO createRepairRequest(CreateRepairRequestDTO dto, String requesterUsername);

    /**
     * Xóa một yêu cầu sửa chữa. Chỉ cho phép xóa khi yêu cầu chưa từng được tạo PCT (workOrders rỗng).
     */
    void deleteRepairRequest(Integer id, String requesterUsername);
}
