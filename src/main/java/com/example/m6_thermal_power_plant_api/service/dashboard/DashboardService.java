package com.example.m6_thermal_power_plant_api.service.dashboard;

import com.example.m6_thermal_power_plant_api.dto.dashboard.DashboardSummaryDTO;
import com.example.m6_thermal_power_plant_api.entity.enums.EquipmentStatus;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus;
import com.example.m6_thermal_power_plant_api.repository.IToolBorrowLogRepository;
import com.example.m6_thermal_power_plant_api.repository.ISparePartInventoryRepository;
import com.example.m6_thermal_power_plant_api.repository.RepairRequestRepository;
import com.example.m6_thermal_power_plant_api.repository.WorkOrderRepository;
import com.example.m6_thermal_power_plant_api.repository.equipment.IEquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service tổng hợp dữ liệu cho Dashboard.
 * Gom tất cả queries vào 1 method → 1 API response.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final IEquipmentRepository equipmentRepo;
    private final RepairRequestRepository repairRequestRepo;
    private final WorkOrderRepository workOrderRepo;
    private final IToolBorrowLogRepository toolBorrowRepo;
    private final ISparePartInventoryRepository sparePartInventoryRepo;

    /** Ngưỡng tồn kho thấp (số lượng) */
    private static final int LOW_STOCK_THRESHOLD = 10;

    /** Số tháng trend chart */
    private static final int TREND_MONTHS = 6;

    /** Số bản ghi top/recent */
    private static final int TOP_LIMIT = 5;

    private static final Map<String, String> STATUS_LABELS = Map.of(
            "ACTIVE", "Đang vận hành",
            "MAINTENANCE", "Đang sửa chữa",
            "FAILURE", "Sự cố",
            "STANDBY", "Dự phòng",
            "RETIRED", "Ngừng hoạt động"
    );

    private static final String[] MONTH_NAMES = {
            "", "T1", "T2", "T3", "T4", "T5", "T6",
            "T7", "T8", "T9", "T10", "T11", "T12"
    };

    public DashboardSummaryDTO getSummary() {
        return DashboardSummaryDTO.builder()
                // KPI Cards
                .totalEquipment(equipmentRepo.count())
                .activeRepairRequests(repairRequestRepo.countActiveRequests())
                .pendingWorkOrders(workOrderRepo.countByStatus(WorkOrderStatus.OPEN))
                .overdueToolBorrows(toolBorrowRepo.countOverdue())
                .lowStockItems(sparePartInventoryRepo.countLowStock(LOW_STOCK_THRESHOLD))
                // Charts & Table
                .equipmentDistribution(buildEquipmentDistribution())
                .repairTrend(buildRepairTrend())
                .topRepairedEquipment(buildTopRepaired())
                .recentRequests(buildRecentRequests())
                .build();
    }

    // ─── Private builders ───

    private List<DashboardSummaryDTO.StatusCountDTO> buildEquipmentDistribution() {
        return equipmentRepo.countByStatusGrouped().stream()
                .map(row -> {
                    String statusStr = row[0] != null
                            ? ((EquipmentStatus) row[0]).name()
                            : "UNKNOWN";
                    return DashboardSummaryDTO.StatusCountDTO.builder()
                            .status(statusStr)
                            .label(STATUS_LABELS.getOrDefault(statusStr, statusStr))
                            .count(((Number) row[1]).longValue())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<DashboardSummaryDTO.MonthlyTrendDTO> buildRepairTrend() {
        return repairRequestRepo.getMonthlyTrend(TREND_MONTHS).stream()
                .map(row -> {
                    int monthNum = ((Number) row[0]).intValue();
                    return DashboardSummaryDTO.MonthlyTrendDTO.builder()
                            .month(monthNum >= 1 && monthNum <= 12 ? MONTH_NAMES[monthNum] : "T?")
                            .totalRequests(((Number) row[1]).longValue())
                            .completedRequests(((Number) row[2]).longValue())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<DashboardSummaryDTO.TopEquipmentDTO> buildTopRepaired() {
        return repairRequestRepo.getTopRepairedEquipment(TOP_LIMIT).stream()
                .map(row -> DashboardSummaryDTO.TopEquipmentDTO.builder()
                        .equipmentName((String) row[0])
                        .repairCount(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    private List<DashboardSummaryDTO.RecentRequestDTO> buildRecentRequests() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
        return repairRequestRepo.findRecentRequests(TOP_LIMIT).stream()
                .map(row -> DashboardSummaryDTO.RecentRequestDTO.builder()
                        .id(row[0] != null ? ((Number) row[0]).intValue() : null)
                        .requestCode(row[1] != null ? row[1].toString() : null)
                        .equipmentName(row[2] != null ? row[2].toString() : null)
                        .kksCode(row[3] != null ? row[3].toString() : null)
                        .priority(row[4] != null ? row[4].toString() : null)
                        .status(row[5] != null ? row[5].toString() : null)
                        .createdAt(row[6] != null
                                ? ((Timestamp) row[6]).toLocalDateTime().format(fmt)
                                : null)
                        .build())
                .collect(Collectors.toList());
    }
}
