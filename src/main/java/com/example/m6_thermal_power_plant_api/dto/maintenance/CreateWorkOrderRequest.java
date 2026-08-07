package com.example.m6_thermal_power_plant_api.dto.maintenance;

import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Body tạo phiếu công tác (PCT) từ một yêu cầu sửa chữa (User Story #40, row 44)
 * HOẶC thủ công nhiều thiết bị (không cần RepairRequest).
 *
 * Chế độ 1 — từ RepairRequest: truyền repairRequestId, thiết bị lấy từ request.
 * Chế độ 2 — thủ công: truyền equipmentIds (danh sách id thiết bị), KHÔNG truyền repairRequestId.
 * Chế độ 3 — bôi trơn: type=LUBRICATION, truyền equipmentLines (thiết bị + plan id).
 * XOR validation: phải cung cấp đúng 1 trong 3, không được cả hai, không được bỏ trống cả hai.
 */
@Getter
@Setter
public class CreateWorkOrderRequest {

    private Integer repairRequestId;

    /** Chế độ WO thủ công nhiều thiết bị (không cần RepairRequest).
     *  Hoặc repairRequestId, hoặc equipmentIds, hoặc equipmentLines — không được cả hai. */
    private List<Integer> equipmentIds;

    /** Loại phiếu: REPAIR (mặc định) / LUBRICATION. null = REPAIR. */
    private WorkOrderType type;

    /** Chế độ WO bôi trơn: danh sách thiết bị kèm plan id (type=LUBRICATION).
     *  REPAIR dùng equipmentIds, LUBRICATION dùng equipmentLines — không cả hai. */
    private List<EquipmentLineInput> equipmentLines;

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

    @AssertTrue(message = "Phai cung cap repairRequestId (tao WO tu yeu cau) HOAC equipmentIds (tao WO thu cong nhieu thiet bi) HOAC equipmentLines (tao WO boi tron), khong duoc ca hai va khong duoc bo trong ca hai")
    public boolean isRequestOrEquipmentProvided() {
        boolean hasRequest = repairRequestId != null;
        boolean hasEquipment = equipmentIds != null && !equipmentIds.isEmpty();
        boolean hasLines = equipmentLines != null && !equipmentLines.isEmpty();
        if (hasLines) {
            return !hasRequest && !hasEquipment;
        }
        return hasRequest ^ hasEquipment;
    }

    @Getter
    @Setter
    public static class MemberInput {

        @NotNull(message = "employeeId của thành viên là bắt buộc")
        private Integer employeeId;

        /** Giờ VÀO khu vực làm việc nhập tay (không truyền = hệ thống lấy now). */
        private LocalDateTime joinedAt;

    }

    @Getter
    @Setter
    public static class EquipmentLineInput {
        private Integer equipmentId;
        private Integer lubricationPlanId;
    }
}
