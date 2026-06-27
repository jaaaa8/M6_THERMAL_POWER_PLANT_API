package com.example.m6_thermal_power_plant_api.dto.maintenance;

import com.example.m6_thermal_power_plant_api.entity.RepairRequest;
import com.example.m6_thermal_power_plant_api.entity.enums.RepairPriority;
import com.example.m6_thermal_power_plant_api.entity.enums.RepairRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Dữ liệu hiển thị một yêu cầu sửa chữa (Request) — dùng cho màn hình
 * "xem danh sách request đang chờ xử lý" (User Story #39, row 43).
 *
 * Map phẳng các quan hệ LAZY (equipment, requester→employee) thành field
 * nguyên thuỷ để tránh lộ entity ra ngoài tầng API và tránh lazy-loading
 * ngoài transaction. {@code from(...)} phải được gọi TRONG transaction.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepairRequestDTO {

    private Integer id;
    private String requestCode;

    private Integer equipmentId;
    private String equipmentKksCode;
    private String equipmentName;

    private Integer requesterId;
    private String requesterUsername;
    private String requesterName;

    private String incidentDescription;
    private RepairPriority priority;
    private RepairRequestStatus status;
    private LocalDateTime createdAt;

    public static RepairRequestDTO from(RepairRequest r) {
        RepairRequestDTOBuilder b = RepairRequestDTO.builder()
                .id(r.getId())
                .requestCode(r.getRequestCode())
                .incidentDescription(r.getIncidentDescription())
                .priority(r.getPriority())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt());

        if (r.getEquipment() != null) {
            b.equipmentId(r.getEquipment().getId())
                    .equipmentKksCode(r.getEquipment().getKksCode())
                    .equipmentName(r.getEquipment().getName());
        }
        if (r.getRequester() != null) {
            b.requesterId(r.getRequester().getId())
                    .requesterUsername(r.getRequester().getUsername());
            if (r.getRequester().getEmployee() != null) {
                b.requesterName(r.getRequester().getEmployee().getFullName());
            }
        }
        return b.build();
    }
}
