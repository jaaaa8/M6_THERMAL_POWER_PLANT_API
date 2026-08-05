package com.example.m6_thermal_power_plant_api.controller.dashboard;

import com.example.m6_thermal_power_plant_api.dto.dashboard.DashboardSummaryDTO;
import com.example.m6_thermal_power_plant_api.service.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint cho Dashboard — trả tổng hợp KPI, chart data, bảng gần nhất.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }
}
