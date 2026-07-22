package com.example.m6_thermal_power_plant_api.dto.maintenance;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Sửa thông tin phiếu công tác đang sống (PATCH /work-orders/{id}).
 *
 * Mọi trường đều TUỲ CHỌN — chỉ trường khác null mới được ghi đè (partial
 * update). Hiện trường nhà máy thay đổi liên tục nên phiếu đang sống được sửa
 * tự do, KHÔNG áp các ràng buộc lúc tạo (trùng vai trò, chồng lấn giờ);
 * phiếu đã COMPLETED/CANCELLED là chứng từ đã chốt — không sửa được.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWorkOrderRequest {

    /** Người lãnh đạo công việc (employeeId). */
    private Integer leaderId;

    /** Chỉ huy trực tiếp (employeeId). */
    private Integer directSupervisorId;

    /** Người giám sát an toàn (employeeId). */
    private Integer safetySupervisorId;

    private LocalDateTime startTime;

    // Không sửa được giờ kết thúc: end_time là mốc THỰC TẾ do hệ thống đóng dấu
    // lúc hoàn thành phiếu (V13).

    /** Mô tả nội dung sửa chữa in trên PCT. */
    private String repairDescription;
}
