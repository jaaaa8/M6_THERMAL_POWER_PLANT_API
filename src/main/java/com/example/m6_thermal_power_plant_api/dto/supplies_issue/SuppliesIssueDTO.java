package com.example.m6_thermal_power_plant_api.dto.supplies_issue;

import com.example.m6_thermal_power_plant_api.dto.consumables.ConsumableIssueDTO;
import com.example.m6_thermal_power_plant_api.dto.spare_parts.SparePartsIssueDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Kết quả của MỘT lần cấp vật tư cho phiếu công tác — gộp cả phần vật tư thay thế
 * (nếu có) và phần vật tư tiêu hao (nếu có) được tạo trong cùng request/transaction.
 *
 * Đúng 1 trong 2 field {@code sparePartsIssue} / {@code consumableIssue} là null khi
 * request chỉ gửi 1 loại vật tư; cả hai được set khi request gửi kèm cả 2 loại.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuppliesIssueDTO {

    /** Id LẦN cấp vật tư vừa tạo (dòng supplies_issues — thực thể cha, V9). */
    private Integer id;
    private Integer workOrderId;
    private String orderCode;
    private java.time.LocalDateTime issuedAt;

    /** Phiếu cấp vật tư thay thế vừa tạo — null nếu request không gửi dòng nào. */
    private SparePartsIssueDTO sparePartsIssue;

    /** Phiếu cấp vật tư tiêu hao vừa tạo — null nếu request không gửi dòng nào. */
    private ConsumableIssueDTO consumableIssue;
}
