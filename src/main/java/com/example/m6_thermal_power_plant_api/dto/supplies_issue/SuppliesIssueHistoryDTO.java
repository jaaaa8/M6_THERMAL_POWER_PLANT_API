package com.example.m6_thermal_power_plant_api.dto.supplies_issue;

import com.example.m6_thermal_power_plant_api.dto.consumables.ConsumableIssueDTO;
import com.example.m6_thermal_power_plant_api.dto.spare_parts.SparePartsIssueDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Toàn bộ lịch sử cấp vật tư (cả thay thế lẫn tiêu hao) của một phiếu công tác,
 * mỗi danh sách đã sắp mới nhất trước.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuppliesIssueHistoryDTO {
    private List<SparePartsIssueDTO> sparePartsIssues;
    private List<ConsumableIssueDTO> consumableIssues;
}
