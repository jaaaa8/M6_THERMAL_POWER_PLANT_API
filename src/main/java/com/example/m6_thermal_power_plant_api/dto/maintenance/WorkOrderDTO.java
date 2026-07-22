package com.example.m6_thermal_power_plant_api.dto.maintenance;

import com.example.m6_thermal_power_plant_api.entity.Employee;
import com.example.m6_thermal_power_plant_api.entity.RepairRequest;
import com.example.m6_thermal_power_plant_api.entity.WorkOrder;
import com.example.m6_thermal_power_plant_api.entity.WorkOrderMember;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Dữ liệu hiển thị một phiếu công tác (PCT). Thông tin thiết bị được lấy từ
 * yêu cầu sửa chữa gắn với phiếu (User Story #40, row 44 — "thông tin thiết bị
 * lấy từ request"). {@code from(...)} phải được gọi TRONG transaction để các
 * quan hệ LAZY (repairRequest, equipment, leader/directSupervisor/safetySupervisor) nạp được.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderDTO {

    private Integer id;
    private String orderCode;
    private WorkOrderStatus status;
    private LocalDateTime startTime;
    /** Kết thúc THỰC TẾ — null khi phiếu chưa COMPLETED. */
    private LocalDateTime endTime;
    private String pdfPath;

    private Integer repairRequestId;
    private String requestCode;

    // Thông tin thiết bị (lấy gián tiếp qua repairRequest → equipment)
    private Integer equipmentId;
    private String equipmentKksCode;
    private String equipmentName;


    private Integer leaderId;
    private String leaderName;
    private Integer directSupervisorId;
    private String directSupervisorName;
    private Integer safetySupervisorId;
    private String safetySupervisorName;
    private String repairDescription;

    private LocalDateTime createdAt;

    private List<WorkOrderMemberDTO> members;

    public static WorkOrderDTO from(WorkOrder wo, List<WorkOrderMember> members) {
        WorkOrderDTOBuilder b = WorkOrderDTO.builder()
                .id(wo.getId())
                .orderCode(wo.getOrderCode())
                .status(wo.getStatus())
                .startTime(wo.getStartTime())
                .endTime(wo.getEndTime())
                .pdfPath(wo.getPdfPath())
                .repairDescription(wo.getRepairDescription());

        RepairRequest req = wo.getRepairRequest();
        if (req != null) {
            b.repairRequestId(req.getId())
                    .requestCode(req.getRequestCode());
            // Phiếu cũ (tạo trước khi có cột repair_description) fallback về mô tả sự cố gốc.
            if (wo.getRepairDescription() == null) {
                b.repairDescription(req.getIncidentDescription());
            }
            if (req.getEquipment() != null) {
                b.equipmentId(req.getEquipment().getId())
                        .equipmentKksCode(req.getEquipment().getKksCode())
                        .equipmentName(req.getEquipment().getName());
            }
            if(req.getCreatedAt() != null) {
                b.createdAt(req.getCreatedAt());
            }else{
                b.createdAt(LocalDateTime.now());
            }
        }

        b.leaderId(idOf(wo.getLeader())).leaderName(nameOf(wo.getLeader()));
        b.directSupervisorId(idOf(wo.getDirectSupervisor())).directSupervisorName(nameOf(wo.getDirectSupervisor()));
        b.safetySupervisorId(idOf(wo.getSafetySupervisor())).safetySupervisorName(nameOf(wo.getSafetySupervisor()));

        if (members != null) {
            b.members(members.stream().map(WorkOrderMemberDTO::from).toList());
        }
        return b.build();
    }

    private static Integer idOf(Employee e) {
        if (e == null) return null;
        try {
            return e.getId();
        } catch (jakarta.persistence.EntityNotFoundException ex) {
            return null;
        }
    }

    private static String nameOf(Employee e) {
        if (e == null) return null;
        try {
            return e.getFullName();
        } catch (jakarta.persistence.EntityNotFoundException ex) {
            return "Nhân viên đã bị xóa";
        }
    }
}
