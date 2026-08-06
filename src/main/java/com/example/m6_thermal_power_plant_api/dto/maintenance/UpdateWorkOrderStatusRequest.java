package com.example.m6_thermal_power_plant_api.dto.maintenance;

import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cập nhật trạng thái phiếu công tác (PATCH /work-orders/{id}/status) — modal
 * "Cập nhật trạng thái" ở danh sách PCT gọi endpoint DUY NHẤT này cho mọi bước:
 *
 *   STOPPED ──mở phiếu ngày──► IN_PROGRESS ──khoá phiếu ngày──► STOPPED
 *      │                            └──khoá phiếu hoàn thành──► COMPLETED
 *      └──huỷ (chỉ khi chưa chạy ngày nào, chỉ người tạo)──► CANCELLED
 *
 * reason là GHI CHÚ tuỳ chọn khi khoá phiếu ngày (target = STOPPED).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWorkOrderStatusRequest {

    @NotNull(message = "targetStatus la bat buoc")
    private WorkOrderStatus targetStatus;

    /** Ghi chú lúc khoá phiếu ngày (targetStatus = STOPPED). Tuỳ chọn. */
    private String reason;
}
