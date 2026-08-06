package com.example.m6_thermal_power_plant_api.dto.maintenance;

import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderEquipmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cập nhật trạng thái làm việc của MỘT thiết bị trong PCT thủ công
 * (PATCH /work-orders/{id}/equipment/{equipmentId}/status).
 * Chỉ nhận IN_PROGRESS / COMPLETED — CANCELED bị chặn ở service (huỷ thiết bị
 * chỉ xảy ra khi huỷ cả phiếu).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEquipmentStatusRequest {

    @NotNull(message = "status la bat buoc")
    private WorkOrderEquipmentStatus status;
}
