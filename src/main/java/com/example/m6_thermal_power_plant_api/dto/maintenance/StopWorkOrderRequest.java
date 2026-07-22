package com.example.m6_thermal_power_plant_api.dto.maintenance;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tổ trưởng gửi duyệt gia hạn phiếu công tác cuối ngày (PATCH /work-orders/{id}/stop).
 *
 * CHỈ có lý do: mỗi lần gia hạn luôn kéo dài đúng 1 ngày và ngày xin mặc định là
 * "ngày mai", nên ngày không cần gửi lên — NGÀY CHO PHÉP LÀM TIẾP do Trưởng ca
 * chốt lúc duyệt (allowed_date, xem MaintenanceService#approveExtension). Lý do
 * được in vào mục "Cho phép làm việc và kết thúc công tác hàng ngày" trên bản
 * giấy PCT để Trưởng ca đọc trước khi ký tay.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StopWorkOrderRequest {

    /** Lý do tạm dừng / xin tiếp tục (VD: "Hết giờ làm việc, xin tiếp tục ngày mai"). */
    @NotBlank(message = "Ly do tam dung khong duoc de trong")
    private String reason;
}
