package com.example.m6_thermal_power_plant_api.dto.maintenance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Tổ trưởng tạm dừng phiếu công tác cuối ngày (PATCH /work-orders/{id}/stop).
 *
 * Cả 2 trường bắt buộc vì chúng được in vào mục "Cho phép làm việc và kết thúc
 * công tác hàng ngày" trên bản giấy PCT — Trưởng ca đọc lý do + ngày xin phép
 * rồi mới ký duyệt tay.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StopWorkOrderRequest {

    /** Lý do tạm dừng / xin tiếp tục (VD: "Hết giờ làm việc, xin tiếp tục ngày mai"). */
    @NotBlank(message = "Ly do tam dung khong duoc de trong")
    private String reason;

    /** Xin phép làm việc đến ngày (extended_until trên work_order_extensions). */
    @NotNull(message = "Ngay xin gia han (extendedUntil) la bat buoc")
    private LocalDateTime extendedUntil;
}
