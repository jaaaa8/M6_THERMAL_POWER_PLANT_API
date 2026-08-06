package com.example.m6_thermal_power_plant_api.dto.maintenance;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Trưởng ca khoá phiếu ngày khi chưa xong việc (PATCH /work-orders/{id}/close-day).
 *
 * Ghi chú KHÔNG bắt buộc — bấm một phát là khoá. Nếu có nhập thì được in vào mục
 * "Cho phép làm việc và kết thúc công tác hàng ngày" trên bản giấy PCT.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StopWorkOrderRequest {

    /** Ghi chú lúc khoá ngày (VD: "Hết giờ làm việc, mai làm tiếp"). Tuỳ chọn. */
    private String reason;
}
