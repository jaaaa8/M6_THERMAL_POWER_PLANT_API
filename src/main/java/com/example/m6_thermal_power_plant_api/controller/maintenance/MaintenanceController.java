package com.example.m6_thermal_power_plant_api.controller.maintenance;

import com.example.m6_thermal_power_plant_api.dto.maintenance.CreateWorkOrderRequest;
import com.example.m6_thermal_power_plant_api.dto.maintenance.RepairRequestDTO;
import com.example.m6_thermal_power_plant_api.dto.maintenance.WorkOrderDTO;
import com.example.m6_thermal_power_plant_api.service.IMaintenanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API cho Quản đốc sửa chữa / Tổ trưởng — Sprint 1 :
 *  - Xem danh sách yêu cầu sửa chữa đang chờ xử lý.
 *  - Tạo phiếu công tác (PCT) từ một yêu cầu.
 */
@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {

    private final IMaintenanceService maintenanceService;

    public MaintenanceController(IMaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    /** Danh sách request đang chờ xử lý (status = PENDING). */
    @GetMapping("/repair-requests/pending")
    public List<RepairRequestDTO> getPendingRepairRequests() {
        return maintenanceService.getPendingRepairRequests();
    }

    /** Tạo phiếu công tác từ một yêu cầu sửa chữa. */
    @PostMapping("/work-orders")
    public ResponseEntity<WorkOrderDTO> createWorkOrder(@Valid @RequestBody CreateWorkOrderRequest request) {
        WorkOrderDTO created = maintenanceService.createWorkOrderFromRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
