package com.example.m6_thermal_power_plant_api.dto.maintenance;

import com.example.m6_thermal_power_plant_api.entity.Employee;
import com.example.m6_thermal_power_plant_api.entity.Equipment;
import com.example.m6_thermal_power_plant_api.entity.RepairRequest;
import com.example.m6_thermal_power_plant_api.entity.WorkOrder;
import com.example.m6_thermal_power_plant_api.entity.WorkOrderEquipment;
import com.example.m6_thermal_power_plant_api.entity.WorkOrderMember;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderEquipmentStatus;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderType;
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
    private WorkOrderType type;
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

    /** Danh sách thiết bị cho WO thủ công (không có RepairRequest) — rỗng cho WO từ request. */
    private List<EquipmentBrief> equipments;

    private Integer leaderId;
    private String leaderName;
    private Integer directSupervisorId;
    private String directSupervisorName;
    private Integer safetySupervisorId;
    private String safetySupervisorName;
    private String repairDescription;

    private LocalDateTime createdAt;

    /**
     * Tài khoản đã tạo phiếu — FE dùng để bật/tắt nút Huỷ (chỉ người tạo mới huỷ
     * được). Chỉ trả id: đọc tên phải khởi tạo proxy LAZY, mà from() còn được gọi
     * ngoài transaction ở luồng danh sách.
     */
    private Integer createdById;

    private List<WorkOrderMemberDTO> members;

    /** Tóm tắt 1 thiết bị trong danh sách equipments của WO thủ công. */
    public record EquipmentBrief(Integer id, String kksCode, String name, String systemName,
                                 WorkOrderEquipmentStatus status, Integer lubricationPlanId) {
        static EquipmentBrief from(WorkOrderEquipment woe) {
            Equipment e = woe.getEquipment();
            return new EquipmentBrief(e.getId(), e.getKksCode(), e.getName(),
                    e.getSystem() != null ? e.getSystem().getName() : null, woe.getStatus(),
                    woe.getLubricationPlan() != null ? woe.getLubricationPlan().getId() : null);
        }
    }

    public static WorkOrderDTO from(WorkOrder wo, List<WorkOrderMember> members) {
        WorkOrderDTOBuilder b = WorkOrderDTO.builder()
                .id(wo.getId())
                .orderCode(wo.getOrderCode())
                .status(wo.getStatus())
                .startTime(wo.getStartTime())
                .endTime(wo.getEndTime())
                .pdfPath(wo.getPdfPath())
                .type(wo.getType())
                .repairDescription(wo.getRepairDescription())
                .createdById(wo.getCreatedBy() != null ? wo.getCreatedBy().getId() : null);

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
        } else {
            // WO thủ công nhiều thiết bị
            b.equipments(wo.getWorkOrderEquipments() == null ? List.of()
                    : wo.getWorkOrderEquipments().stream().map(EquipmentBrief::from).toList());
        }

        // Sửa bug createdAt: WO thủ công không có req.getCreatedAt() → dùng wo.getCreatedAt()
        b.createdAt(wo.getCreatedAt() != null ? wo.getCreatedAt()
                : (req != null && req.getCreatedAt() != null ? req.getCreatedAt() : LocalDateTime.now()));

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
