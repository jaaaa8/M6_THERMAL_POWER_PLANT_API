package com.example.m6_thermal_power_plant_api.dto.spare_parts;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Body tạo phiếu cấp vật tư thay thế cho một phiếu công tác
 * (POST /api/v1/work-orders/{workOrderId}/spare-parts-issues).
 *
 * 1 phiếu có NHIỀU dòng vật tư (spare_parts_issue_details). Phiếu chỉ là YÊU CẦU
 * cấp vật tư — KHÔNG trừ tồn kho; việc xuất kho thật (SparePartExport + giao dịch
 * EXPORT trong spare_parts_inventory) là bước sau do thủ kho thực hiện.
 *
 * Mã phiếu (spare_part_code) do backend tự sinh qua TimeStampCodeGenerator,
 * người dùng KHÔNG truyền. Người cấp phát (issuedBy) lấy từ tài khoản đăng nhập.
 */
@Getter
@Setter
public class CreateSparePartsIssueRequest {

    @Valid
    @NotEmpty(message = "Phiếu cấp vật tư phải có ít nhất 1 dòng vật tư")
    private List<Line> items;

    @Getter
    @Setter
    public static class Line {

        @NotNull(message = "sparePartId là bắt buộc")
        private Integer sparePartId;

        @NotNull(message = "quantity là bắt buộc")
        @Positive(message = "quantity phải lớn hơn 0")
        private BigDecimal quantity;
    }
}
