package com.example.m6_thermal_power_plant_api.dto.supplies_issue;

import com.example.m6_thermal_power_plant_api.dto.consumables.ConsumableIssueDTO;
import com.example.m6_thermal_power_plant_api.dto.spare_parts.SparePartsIssueDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * MỘT LẦN cấp vật tư trong lịch sử của một phiếu công tác (dòng supplies_issues)
 * — hiển thị "Phiếu cấp vật tư #seq — thời điểm — người cấp" kèm phần vật tư
 * thay thế / tiêu hao (mỗi phần có thể null nếu lần đó chỉ cấp 1 loại).
 *
 * {@code id} = null với dữ liệu mồ côi (phiếu con cũ chưa gắn được lần cấp cha)
 * — các lần này KHÔNG xuất PDF riêng được.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuppliesIssueBatchDTO {

    private Integer id;
    /** Số thứ tự lần cấp trong phiếu công tác, đánh theo thời gian tăng dần (từ 1). */
    private Integer seq;
    private LocalDateTime issuedAt;
    private Integer issuedById;
    private String issuedByName;

    private SparePartsIssueDTO sparePartsIssue;
    private ConsumableIssueDTO consumableIssue;
}
