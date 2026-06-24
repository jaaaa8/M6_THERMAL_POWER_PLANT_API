package com.example.m6_thermal_power_plant_api.service;

import com.example.m6_thermal_power_plant_api.dto.maintenance.CreateWorkOrderRequest;
import com.example.m6_thermal_power_plant_api.dto.maintenance.RepairRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.WorkOrderDTO;

import java.util.List;

/**
 * Nghiệp vụ cho Quản đốc sửa chữa / Tổ trưởng.
 */
public interface IMaintenanceService {

    /**
     * User Story #39 (row 43): xem danh sách các yêu cầu sửa chữa đang chờ xử lý
     * (status = PENDING), mới nhất lên trước.
     */
    List<RepairRequestDTO> getPendingRepairRequests();

    /**
     * User Story #40 (row 44): tạo một phiếu công tác (PCT) từ 1 yêu cầu sửa chữa.
     * Thông tin thiết bị lấy từ request; gắn người lãnh đạo công việc, chỉ huy
     * trực tiếp, người giám sát an toàn và các nhân viên làm việc.
     */
    WorkOrderDTO createWorkOrderFromRequest(CreateWorkOrderRequest request);
}
