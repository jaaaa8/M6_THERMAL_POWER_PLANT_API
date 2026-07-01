package com.example.m6_thermal_power_plant_api.controller.repair;

import com.example.m6_thermal_power_plant_api.dto.maintenance.RepairRequestDTO;
import com.example.m6_thermal_power_plant_api.service.maintenance.IMaintenanceService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/repair-requests")
public class RepairRequestController {
    private final IMaintenanceService maintenanceService;

    public RepairRequestController(IMaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }


    /**
     * Danh sách request đang chờ xử lý (status = PENDING), CÓ PHÂN TRANG.
     * Tham số query chuẩn của Spring: {@code ?page=0&size=20&sort=createdAt,desc}.
     * Mặc định trang 20 dòng, sắp xếp createdAt giảm dần (mới nhất lên trước).
     *
     * Trả {@link PagedModel} (bọc quanh Page) để JSON phân trang ổn định, tránh
     * cảnh báo "serializing PageImpl is not supported" của Spring Boot 3.x. JSON
     * gồm {@code content[]} + khối {@code page} (size, number, totalElements,
     * totalPages).
     */
    @GetMapping("/pending")
    public PagedModel<RepairRequestDTO> getPendingRepairRequests(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(maintenanceService.getPendingRepairRequests(pageable));
    }
}
