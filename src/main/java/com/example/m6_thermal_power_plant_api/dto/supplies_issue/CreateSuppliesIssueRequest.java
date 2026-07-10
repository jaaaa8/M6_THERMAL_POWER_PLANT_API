package com.example.m6_thermal_power_plant_api.dto.supplies_issue;

import com.example.m6_thermal_power_plant_api.dto.consumables.CreateConsumableIssueRequest;
import com.example.m6_thermal_power_plant_api.dto.spare_parts.CreateSparePartsIssueRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Body tạo phiếu cấp vật tư cho một phiếu công tác
 * (POST /api/v1/work-orders/{workOrderId}/supplies-issues).
 *
 * Một phiếu công tác thường cần CẢ vật tư thay thế lẫn vật tư tiêu hao, nên phiếu
 * cấp vật tư gộp cả hai loại vào một hành động: gửi kèm dòng nào thì phiếu tương
 * ứng (spare-parts-issue / consumable-issue) được tạo, có thể gửi 1 hoặc cả 2 loại
 * trong cùng 1 request — cả hai được tạo trong CÙNG một giao dịch (transaction).
 *
 * Dữ liệu vẫn lưu ở 2 bảng gốc (spare_parts_issues / consumable_issues) — đây là
 * lớp API/nghiệp vụ gộp lại, KHÔNG đổi schema.
 */
@Getter
@Setter
public class CreateSuppliesIssueRequest {

    @Valid
    private List<CreateSparePartsIssueRequest.Line> spareParts;

    @Valid
    private List<CreateConsumableIssueRequest.Line> consumables;

    @AssertTrue(message = "Phiếu cấp vật tư phải có ít nhất 1 dòng vật tư thay thế hoặc vật tư tiêu hao")
    public boolean isHasAtLeastOneItem() {
        return (spareParts != null && !spareParts.isEmpty())
                || (consumables != null && !consumables.isEmpty());
    }
}
