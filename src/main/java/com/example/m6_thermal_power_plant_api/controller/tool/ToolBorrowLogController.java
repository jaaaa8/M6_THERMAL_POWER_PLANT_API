package com.example.m6_thermal_power_plant_api.controller.tool;

import com.example.m6_thermal_power_plant_api.dto.tool.ApiResponse;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolBorrowLogResponse;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolBorrowRejectRequest;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolBorrowRequest;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolBorrowReturnRequest;
import com.example.m6_thermal_power_plant_api.entity.enums.BorrowStatus;
import com.example.m6_thermal_power_plant_api.service.ToolBorrowOverdueNotifier;
import com.example.m6_thermal_power_plant_api.service.impl.IToolBorrowLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tool-borrow-logs")
@RequiredArgsConstructor
public class ToolBorrowLogController {

    private final IToolBorrowLogService toolBorrowLogService;
    private final ToolBorrowOverdueNotifier toolBorrowOverdueNotifier;

    /** Nhân sự đăng ký mượn CCDC */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ToolBorrowLogResponse> createBorrowRequest(
            @RequestParam Integer accountId,
            @Valid @RequestBody ToolBorrowRequest request) {
        return ApiResponse.success("Tạo phiếu mượn thành công", toolBorrowLogService.createBorrowRequest(accountId, request));
    }

    /** Thủ kho duyệt và giao CCDC */
    @PatchMapping("/{id}/approve")
    public ApiResponse<ToolBorrowLogResponse> approve(@PathVariable Integer id,
                                                        @RequestParam Integer approvedByAccountId) {
        return ApiResponse.success("Duyệt phiếu mượn thành công", toolBorrowLogService.approve(id, approvedByAccountId));
    }

    /** Thủ kho từ chối phiếu mượn */
    @PatchMapping("/{id}/reject")
    public ApiResponse<ToolBorrowLogResponse> reject(@PathVariable Integer id,
                                                       @RequestParam Integer approvedByAccountId,
                                                       @RequestBody ToolBorrowRejectRequest request) {
        return ApiResponse.success("Từ chối phiếu mượn thành công", toolBorrowLogService.reject(id, approvedByAccountId, request));
    }

    /** Thủ kho xác nhận nhận lại CCDC */
    @PatchMapping("/{id}/return")
    public ApiResponse<ToolBorrowLogResponse> returnTool(@PathVariable Integer id,
                                                           @Valid @RequestBody ToolBorrowReturnRequest request) {
        return ApiResponse.success("Trả CCDC thành công", toolBorrowLogService.returnTool(id, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ToolBorrowLogResponse> getById(@PathVariable Integer id) {
        return ApiResponse.success(toolBorrowLogService.getById(id));
    }

    @GetMapping
    public ApiResponse<Page<ToolBorrowLogResponse>> search(
            @RequestParam(required = false) Integer accountId,
            @RequestParam(required = false) Integer toolId,
            @RequestParam(required = false) BorrowStatus status,
            Pageable pageable) {
        return ApiResponse.success(toolBorrowLogService.search(accountId, toolId, status, pageable));
    }

    /** Thủ kho bấm gửi ngay email nhắc quá hạn, không cần chờ job chạy theo giờ */
    @PostMapping("/notify-overdue")
    public ApiResponse<Integer> notifyOverdueNow() {
        int sentCount = toolBorrowOverdueNotifier.sendOverdueNotifications();
        return ApiResponse.success("Đã gửi " + sentCount + " email nhắc quá hạn", sentCount);
    }
}
