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

    private Integer repairRequestId;

    @NotNull(message = "leaderId (người lãnh đạo công việc) là bắt buộc")
    private Integer leaderId;

    @NotNull(message = "directSupervisorId (chỉ huy trực tiếp) là bắt buộc")
    private Integer directSupervisorId;

    @NotNull(message = "safetySupervisorId (người giám sát an toàn) là bắt buộc")
    private Integer safetySupervisorId;

    /** Thời điểm bắt đầu công việc. */
    @NotNull(message = "startTime là bắt buộc.")
    private LocalDateTime startTime;

    // KHÔNG có mốc kết thúc: work_orders.end_time là giờ kết thúc THỰC TẾ, chỉ
    // được ghi khi phiếu COMPLETED (V13) — không nhập lúc tạo nữa.

    private String repairDescription;

    private LocalDateTime createdAt;

    @Valid
    private List<MemberInput> members;

    @Getter
    @Setter
    public static class MemberInput {

        @NotNull(message = "employeeId của thành viên là bắt buộc")
        private Integer employeeId;

    }
}
