package com.example.m6_thermal_power_plant_api.service.consumable;

import com.example.m6_thermal_power_plant_api.dto.consumables.ConsumableIssueDTO;
import com.example.m6_thermal_power_plant_api.dto.consumables.CreateConsumableIssueRequest;
import com.example.m6_thermal_power_plant_api.entity.SuppliesIssue;

import java.util.List;

public interface IConsumableIssuesService {

    /**
     * Tạo phiếu cấp vật tư tiêu hao (nhiều dòng chi tiết) cho một phiếu công tác.
     *
     * @param workOrderId       phiếu công tác nhận vật tư (phải đang OPEN/IN_PROGRESS)
     * @param request           danh sách dòng vật tư (consumableId + quantity)
     * @param issuedByUsername  username tài khoản đăng nhập đang thao tác (người cấp phát)
     * @param suppliesIssue     LẦN cấp vật tư cha (bảng supplies_issues) — null khi
     *                          gọi trực tiếp ngoài luồng gộp (endpoint cũ)
     */
    ConsumableIssueDTO createForWorkOrder(Integer workOrderId, CreateConsumableIssueRequest request,
                                          String issuedByUsername, SuppliesIssue suppliesIssue);

    /** Như trên nhưng không gắn LẦN cấp cha (giữ tương thích endpoint/luồng cũ). */
    default ConsumableIssueDTO createForWorkOrder(Integer workOrderId, CreateConsumableIssueRequest request,
                                                  String issuedByUsername) {
        return createForWorkOrder(workOrderId, request, issuedByUsername, null);
    }

    /** Danh sách phiếu cấp vật tư tiêu hao của một phiếu công tác (kèm dòng chi tiết), mới nhất trước. */
    List<ConsumableIssueDTO> getByWorkOrder(Integer workOrderId);
}
