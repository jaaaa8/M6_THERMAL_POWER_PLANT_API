package com.example.m6_thermal_power_plant_api.dto.maintenance;

import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.WorkOrderExtension;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Một dòng gia hạn / tạm dừng cuối ngày của phiếu công tác.
 * approvedBy null = đang chờ Trưởng ca ký duyệt bản giấy (WAITING_FOR_APPROVAL);
 * khác null = người đã xác nhận online SAU KHI cầm bản giấy có chữ ký Trưởng ca.
 * {@code from(...)} phải gọi TRONG transaction để nạp quan hệ LAZY approvedBy.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderExtensionDTO {

    private Integer id;
    private String reason;
    /** Lúc Tổ trưởng gửi duyệt gia hạn. */
    private LocalDateTime requestedAt;
    /** Ngày Trưởng ca cho phép làm tiếp — null khi chưa duyệt. */
    private LocalDate allowedDate;
    private Integer approvedById;
    private String approvedByName;

    public static WorkOrderExtensionDTO from(WorkOrderExtension extension) {
        Account approvedBy = extension.getApprovedBy();
        String approvedByName = null;
        if (approvedBy != null) {
            approvedByName = approvedBy.getEmployee() != null && approvedBy.getEmployee().getFullName() != null
                    ? approvedBy.getEmployee().getFullName()
                    : approvedBy.getUsername();
        }
        return WorkOrderExtensionDTO.builder()
                .id(extension.getId())
                .reason(extension.getReason())
                .requestedAt(extension.getRequestedAt())
                .allowedDate(extension.getAllowedDate())
                .approvedById(approvedBy != null ? approvedBy.getId() : null)
                .approvedByName(approvedByName)
                .build();
    }
}
