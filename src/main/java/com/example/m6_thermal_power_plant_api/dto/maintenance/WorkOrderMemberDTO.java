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
    private Integer accountId;
    private String username;
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

        if (m.getAccount() != null) {
            b.accountId(m.getAccount().getId())
                    .username(m.getAccount().getUsername());
            if (m.getAccount().getEmployee() != null) {
                b.fullName(m.getAccount().getEmployee().getFullName());
            }
        }
        return b.build();
    }
}
