package com.example.m6_thermal_power_plant_api.service.impl;

import com.example.m6_thermal_power_plant_api.dto.tool.ToolBorrowLogResponse;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolBorrowRejectRequest;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolBorrowRequest;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolBorrowReturnRequest;
import com.example.m6_thermal_power_plant_api.entity.enums.BorrowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IToolBorrowLogService {

    /** Nhân sự đăng ký mượn CCDC -> tạo phiếu trạng thái PENDING */
    ToolBorrowLogResponse createBorrowRequest(Integer accountId, ToolBorrowRequest request);

    /** Thủ kho duyệt và xác nhận giao CCDC -> APPROVED, cộng quantityBorrowed */
    ToolBorrowLogResponse approve(Integer id, Integer approvedByAccountId);

    /** Thủ kho từ chối phiếu mượn -> REJECTED */
    ToolBorrowLogResponse reject(Integer id, Integer approvedByAccountId, ToolBorrowRejectRequest request);

    /** Thủ kho xác nhận trả CCDC -> RETURNED, trừ quantityBorrowed, cộng quantityDamaged nếu có hư hỏng */
    ToolBorrowLogResponse returnTool(Integer id, ToolBorrowReturnRequest request);

    ToolBorrowLogResponse getById(Integer id);

    Page<ToolBorrowLogResponse> search(Integer accountId, Integer toolId, BorrowStatus status, Pageable pageable);
}
