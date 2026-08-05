package com.example.m6_thermal_power_plant_api.controller.repair;

import com.example.m6_thermal_power_plant_api.dto.maintenance.RepairRequestDTO;
import com.example.m6_thermal_power_plant_api.service.maintenance.IRepairService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/repair-requests")
public class RepairRequestController {
    private final IRepairService repairService;

    public RepairRequestController(IRepairService repairService) {
        this.repairService = repairService;
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
     *
     * Dùng để Quản đốc sửa chữa / Tổ trưởng chọn request tạo Phiếu công tác.
     */
    @PreAuthorize("hasAnyRole('MAINTENANCE_FOREMAN', 'TEAM_LEADER', 'SHIFT_LEADER', 'CREW_LEADER')")
    @GetMapping("/pending")
    public PagedModel<RepairRequestDTO> getPendingRepairRequests(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(repairService.getPendingRepairRequests(pageable));
    }

    @PreAuthorize("hasAnyRole('SHIFT_LEADER', 'CREW_LEADER', 'MAINTENANCE_FOREMAN', 'TEAM_LEADER')")
    @GetMapping
    public PagedModel<RepairRequestDTO> getAllRepairRequests(
            @RequestParam(required = false) com.example.m6_thermal_power_plant_api.entity.enums.RepairRequestStatus status,
            @RequestParam(required = false) com.example.m6_thermal_power_plant_api.entity.enums.RepairPriority priority,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(repairService.getAllRepairRequests(status, priority, search, pageable));
    }

    /** Số liệu tổng hợp cho stat cards + pill counts (đếm trên toàn bộ, không phụ thuộc trang). */
    @PreAuthorize("hasAnyRole('SHIFT_LEADER', 'CREW_LEADER', 'MAINTENANCE_FOREMAN', 'TEAM_LEADER')")
    @GetMapping("/stats")
    public com.example.m6_thermal_power_plant_api.dto.maintenance.RepairRequestStatsDTO getStats() {
        return repairService.getStats();
    }

    @PreAuthorize("hasAnyRole('SHIFT_LEADER', 'CREW_LEADER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RepairRequestDTO createRepairRequest(
            @Valid @RequestBody com.example.m6_thermal_power_plant_api.dto.maintenance.CreateRepairRequestDTO dto,
            Authentication authentication) {
        return repairService.createRepairRequest(dto, authentication.getName());
    }

    @PreAuthorize("hasAnyRole('SHIFT_LEADER', 'CREW_LEADER')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRepairRequest(@PathVariable Integer id, Authentication authentication) {
        repairService.deleteRepairRequest(id, authentication.getName());
    }
}
