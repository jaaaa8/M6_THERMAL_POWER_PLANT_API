package com.example.m6_thermal_power_plant_api.dto.consumables;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Body tạo phiếu cấp vật tư tiêu hao cho một phiếu công tác
 * (POST /api/v1/work-orders/{workOrderId}/consumable-issues).
 *
 * 1 phiếu có NHIỀU dòng vật tư (consumable_issue_details). Phiếu chỉ là YÊU CẦU
 * cấp vật tư — KHÔNG trừ tồn kho; việc xuất kho thật (ConsumableExport + giao dịch
 * EXPORT trong consumable_inventory) là bước sau do thủ kho thực hiện.
 *
 * Mã phiếu (consumable_code) do backend tự sinh qua TimeStampCodeGenerator,
 * người dùng KHÔNG truyền. Người cấp phát (issuedBy) lấy từ tài khoản đăng nhập.
 */
@Getter
@Setter
public class CreateConsumableIssueRequest {

    @Valid
    @NotEmpty(message = "Phiếu cấp vật tư phải có ít nhất 1 dòng vật tư")
    private List<Line> items;

    @Getter
    @Setter
    public static class Line {

        @NotNull(message = "consumableId là bắt buộc")
        private Integer consumableId;

        @NotNull(message = "quantity là bắt buộc")
        @Positive(message = "quantity phải lớn hơn 0")
        private BigDecimal quantity;
    }
}
