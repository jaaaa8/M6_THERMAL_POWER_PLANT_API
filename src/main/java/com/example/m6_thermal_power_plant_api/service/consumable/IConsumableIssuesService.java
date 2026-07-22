package com.example.m6_thermal_power_plant_api.service.consumable;

import com.example.m6_thermal_power_plant_api.dto.consumables.ConsumableIssueDTO;
import com.example.m6_thermal_power_plant_api.dto.consumables.CreateConsumableIssueRequest;
import com.example.m6_thermal_power_plant_api.entity.enums.ConsumableIssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IConsumableIssuesService {

    /**
     * Tạo phiếu cấp vật tư tiêu hao (nhiều dòng chi tiết) cho một phiếu công tác.
     *
     * @param workOrderId       phiếu công tác nhận vật tư (phải đang OPEN/IN_PROGRESS)
     * @param request           danh sách dòng vật tư (consumableId + quantity)
     * @param issuedByUsername  username tài khoản đăng nhập đang thao tác (người cấp phát)
     */
    ConsumableIssueDTO createForWorkOrder(Integer workOrderId, CreateConsumableIssueRequest request,
                                          String issuedByUsername);

    /** Danh sách phiếu cấp vật tư tiêu hao của một phiếu công tác (kèm dòng chi tiết), mới nhất trước. */
    List<ConsumableIssueDTO> getByWorkOrder(Integer workOrderId);

    Page<ConsumableIssueDTO> search(String keyword, ConsumableIssueStatus status, Pageable pageable);

    ConsumableIssueDTO findById(Integer id);

    ConsumableIssueDTO update(ConsumableIssueDTO dto);

    ConsumableIssueDTO uploadSignedPdf(Integer id, MultipartFile file);
}
