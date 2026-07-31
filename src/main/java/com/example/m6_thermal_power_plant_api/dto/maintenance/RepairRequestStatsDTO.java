package com.example.m6_thermal_power_plant_api.dto.maintenance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Số liệu tổng hợp cho màn hình Yêu cầu sửa chữa. Đếm trên TOÀN BỘ bản ghi
 * (không phụ thuộc trang đang xem) nên pill counts + stat cards luôn khớp
 * {@code total}. {@code emergencyPending} là TẬP CON của {@code pending}
 * (ưu tiên EMERGENCY & đang chờ xử lý) — KHÔNG cộng dồn vào total.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepairRequestStatsDTO {
    private long total;
    private long pending;
    private long approved;
    private long inProgress;
    private long completed;
    private long emergencyPending;
}
