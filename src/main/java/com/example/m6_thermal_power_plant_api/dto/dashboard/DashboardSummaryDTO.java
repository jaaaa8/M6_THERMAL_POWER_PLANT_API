package com.example.m6_thermal_power_plant_api.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO tổng hợp dữ liệu Dashboard — trả về từ GET /api/dashboard/summary.
 * Gom tất cả KPI, chart data, table data vào 1 response duy nhất.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {

    // === KPI Cards ===
    private long totalEquipment;
    private long activeRepairRequests;
    private long pendingWorkOrders;
    private long overdueToolBorrows;
    private long lowStockItems;

    // === Equipment Distribution (Pie Chart) ===
    private List<StatusCountDTO> equipmentDistribution;

    // === Repair Trend — 6 tháng (Area Chart) ===
    private List<MonthlyTrendDTO> repairTrend;

    // === Top thiết bị sửa nhiều nhất (Bar Chart) ===
    private List<TopEquipmentDTO> topRepairedEquipment;

    // === Yêu cầu sửa chữa gần đây (Table) ===
    private List<RecentRequestDTO> recentRequests;

    // ─── Nested DTOs ───

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusCountDTO {
        private String status;
        private String label;
        private long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTrendDTO {
        private String month;
        private long totalRequests;
        private long completedRequests;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopEquipmentDTO {
        private String equipmentName;
        private long repairCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentRequestDTO {
        private Integer id;
        private String requestCode;
        private String equipmentName;
        private String kksCode;
        private String priority;
        private String status;
        private String createdAt;
    }
}
