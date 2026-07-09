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
 *
 * {@code issues} là cùng dữ liệu đó nhưng GOM THEO TỪNG LẦN cấp (bảng cha
 * supplies_issues, cũ nhất trước để đánh số #1, #2...) — UI hiển thị lịch sử
 * và xuất PDF theo từng lần dựa vào danh sách này; 2 danh sách phẳng giữ lại
 * cho các nơi tiêu thụ cũ.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuppliesIssueHistoryDTO {
    private List<SparePartsIssueDTO> sparePartsIssues;
    private List<ConsumableIssueDTO> consumableIssues;
    private List<SuppliesIssueBatchDTO> issues;
}
