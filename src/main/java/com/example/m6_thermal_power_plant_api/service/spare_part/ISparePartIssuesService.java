package com.example.m6_thermal_power_plant_api.service.spare_part;

import com.example.m6_thermal_power_plant_api.dto.spare_parts.CreateSparePartsIssueRequest;
import com.example.m6_thermal_power_plant_api.dto.spare_parts.SparePartsIssueDTO;

import java.util.List;

public interface ISparePartIssuesService {

    /**
     * Tạo phiếu cấp vật tư thay thế (nhiều dòng chi tiết) cho một phiếu công tác.
     *
     * @param workOrderId       phiếu công tác nhận vật tư (phải đang OPEN/IN_PROGRESS)
     * @param request           danh sách dòng vật tư (sparePartId + quantity)
     * @param issuedByUsername  username tài khoản đăng nhập đang thao tác (người cấp phát)
     */
    SparePartsIssueDTO createForWorkOrder(Integer workOrderId, CreateSparePartsIssueRequest request,
                                          String issuedByUsername);

    /** Danh sách phiếu cấp vật tư của một phiếu công tác (kèm dòng chi tiết), mới nhất trước. */
    List<SparePartsIssueDTO> getByWorkOrder(Integer workOrderId);
}
