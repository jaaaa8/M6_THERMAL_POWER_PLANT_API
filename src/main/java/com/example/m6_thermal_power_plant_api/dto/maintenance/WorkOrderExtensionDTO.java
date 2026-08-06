package com.example.m6_thermal_power_plant_api.dto.maintenance;

import com.example.m6_thermal_power_plant_api.entity.WorkOrderExtension;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Một NGÀY công tác của phiếu công tác (nhật ký hàng ngày).
 * closedAt null = ngày công tác đang mở, đội vẫn đang làm việc.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderExtensionDTO {

    private Integer id;
    /** Ghi chú lúc khoá phiếu ngày (tuỳ chọn). */
    private String reason;
    /** Giờ mở phiếu ngày. */
    private LocalDateTime requestedAt;
    /** Giờ khoá phiếu ngày — null khi ngày công tác còn đang mở. */
    private LocalDateTime closedAt;
    /** Ngày công tác. */
    private LocalDate allowedDate;

    public static WorkOrderExtensionDTO from(WorkOrderExtension extension) {
        return WorkOrderExtensionDTO.builder()
                .id(extension.getId())
                .reason(extension.getReason())
                .requestedAt(extension.getRequestedAt())
                .closedAt(extension.getClosedAt())
                .allowedDate(extension.getAllowedDate())
                .build();
    }
}
