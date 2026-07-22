package com.example.m6_thermal_power_plant_api.dto.maintenance;

import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Cập nhật trạng thái phiếu công tác (PATCH /work-orders/{id}/status) — modal
 * "Cập nhật trạng thái" ở danh sách PCT gọi endpoint DUY NHẤT này cho mọi bước:
 *
 *   OPEN (chờ duyệt) ──duyệt phiếu──► APPROVED ──bắt đầu──► IN_PROGRESS
 *   IN_PROGRESS ──hết ngày, không kịp──► STOPPED ──gửi duyệt lại──►
 *   WAITING_FOR_APPROVAL ──duyệt gia hạn──► APPROVED ──► ... ──► COMPLETED
 *   (mọi trạng thái sống ──huỷ──► CANCELLED)
 *
 * reason chỉ bắt buộc khi target = WAITING_FOR_APPROVAL (được in vào mục gia hạn
 * trên bản giấy PCT đưa Trưởng ca ký); allowedDate chỉ dùng khi target =
 * APPROVED từ WAITING_FOR_APPROVAL — Trưởng ca chốt ngày cho làm tiếp.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWorkOrderStatusRequest {

    @NotNull(message = "targetStatus la bat buoc")
    private WorkOrderStatus targetStatus;

    /** Lý do xin gia hạn — bắt buộc khi targetStatus = WAITING_FOR_APPROVAL. */
    private String reason;

    /**
     * NGÀY Trưởng ca cho phép làm tiếp — chỉ đọc khi duyệt gia hạn (targetStatus
     * = APPROVED trên phiếu đang WAITING_FOR_APPROVAL). Bỏ trống = ngày hôm sau
     * ngày gửi duyệt.
     */
    private LocalDate allowedDate;
}
