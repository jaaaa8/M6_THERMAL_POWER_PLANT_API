package com.example.m6_thermal_power_plant_api.dto.maintenance;

import com.example.m6_thermal_power_plant_api.entity.WorkOrderMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Thành viên tham gia một phiếu công tác (PCT). */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderMemberDTO {

    private Integer id;
    private Integer employeeId;
    private String fullName;
    private String roleInTask;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;

    public static WorkOrderMemberDTO from(WorkOrderMember m) {
        WorkOrderMemberDTOBuilder b = WorkOrderMemberDTO.builder()
                .id(m.getId())
                .roleInTask(m.getRoleInTask())
                .joinedAt(m.getJoinedAt())
                .leftAt(m.getLeftAt());

        if (m.getEmployees() != null) {
            try {
                b.employeeId(m.getEmployees().getId());
                b.fullName(m.getEmployees().getFullName());
            } catch (jakarta.persistence.EntityNotFoundException e) {
                b.fullName("Nhân viên đã bị xóa");
            }
        }
        return b.build();
    }
}
