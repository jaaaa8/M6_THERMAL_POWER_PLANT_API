package com.example.m6_thermal_power_plant_api.dto.maintenance;

import com.example.m6_thermal_power_plant_api.dto.spare_parts.SparePartsIssueDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Chi tiết đầy đủ một phiếu công tác (GET /api/v1/work-orders/{id}):
 *  - {@code workOrder}     : toàn bộ thông tin chung + danh sách members hiện có
 *                            (mỗi member kèm joinedAt/leftAt — leftAt null = đang
 *                            trong khu vực làm việc, khác null = đã rời).
 *  - {@code memberHistory} : dòng thời gian ra/vào, sắp xếp theo thời gian TĂNG dần
 *                            (xem {@link MemberHistoryEventDTO}).
 *  - {@code sparePartsIssues}: các phiếu cấp vật tư thay thế đã tạo cho phiếu này.
 *  - {@code extensions}    : các lần tạm dừng cuối ngày / gia hạn (approvedBy null
 *                            = đang chờ Trưởng ca duyệt bản giấy), theo ngày tăng dần.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderDetailDTO {

    private WorkOrderDTO workOrder;
    private List<MemberHistoryEventDTO> memberHistory;
    private List<SparePartsIssueDTO> sparePartsIssues;
    private List<WorkOrderExtensionDTO> extensions;
}
