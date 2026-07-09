package com.example.m6_thermal_power_plant_api.service.supplies_issue;

import com.example.m6_thermal_power_plant_api.dto.supplies_issue.CreateSuppliesIssueRequest;
import com.example.m6_thermal_power_plant_api.dto.supplies_issue.SuppliesIssueDTO;
import com.example.m6_thermal_power_plant_api.dto.supplies_issue.SuppliesIssueHistoryDTO;

public interface ISuppliesIssueService {

    /**
     * Tạo phiếu cấp vật tư cho một phiếu công tác — gộp vật tư thay thế và vật tư
     * tiêu hao trong CÙNG một hành động/giao dịch. Request có thể gửi 1 hoặc cả 2
     * loại dòng vật tư.
     *
     * @param workOrderId       phiếu công tác nhận vật tư (phải đang OPEN/IN_PROGRESS)
     * @param request           danh sách dòng vật tư thay thế và/hoặc tiêu hao
     * @param issuedByUsername  username tài khoản đăng nhập đang thao tác (người cấp phát)
     */
    SuppliesIssueDTO createForWorkOrder(Integer workOrderId, CreateSuppliesIssueRequest request,
                                        String issuedByUsername);

    /** Toàn bộ lịch sử cấp vật tư (cả 2 loại) của một phiếu công tác. */
    SuppliesIssueHistoryDTO getByWorkOrder(Integer workOrderId);
}
