package com.example.m6_thermal_power_plant_api.dto.maintenance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Body tạo phiếu công tác (PCT) từ một yêu cầu sửa chữa (User Story #40, row 44).
 *
 * Thông tin thiết bị KHÔNG truyền ở đây — nó được lấy từ chính request
 * ({@code repairRequestId}). Người dùng chỉ chọn nhân sự tham gia:
 *  - leaderId            : người lãnh đạo công việc (bắt buộc)
 *  - directSupervisorId  : chỉ huy trực tiếp (tuỳ chọn)
 *  - safetySupervisorId  : người giám sát an toàn (tuỳ chọn)
 *  - members             : nhân viên làm việc (tuỳ chọn)
 */
@Getter
@Setter
public class CreateWorkOrderRequest {

    @NotNull(message = "repairRequestId là bắt buộc")
    private Integer repairRequestId;

    @NotNull(message = "leaderId (người lãnh đạo công việc) là bắt buộc")
    private Integer leaderId;

    private Integer directSupervisorId;

    private Integer safetySupervisorId;

    /** Thời điểm bắt đầu công việc; để trống nếu chưa xác định. */
    private LocalDateTime startTime;

    /**
     * Thời điểm dự kiến kết thúc công việc; để trống nếu chưa xác định.
     *
     * BẮT BUỘC (cùng startTime) khi yêu cầu này ĐÃ có một phiếu công tác đang
     * hoạt động (OPEN/IN_PROGRESS) — để hệ thống kiểm tra khung giờ KHÔNG chồng
     * lấn giữa các phiếu song song (xem ràng buộc ở MaintenanceService).
     */
    private LocalDateTime expectedEndTime;

    @Valid
    private List<MemberInput> members;

    @Getter
    @Setter
    public static class MemberInput {

        @NotNull(message = "accountId của thành viên là bắt buộc")
        private Integer accountId;

        /** Vai trò trong công việc (VD: Thợ cơ khí, Thợ điện...). */
        private String roleInTask;
    }
}
