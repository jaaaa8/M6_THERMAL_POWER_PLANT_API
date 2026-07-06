package com.example.m6_thermal_power_plant_api.dto.maintenance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Một sự kiện trong lịch sử ra/vào phiếu công tác, hiển thị dạng dòng thời gian:
 *   "Employee A left at 05:00 AM" → "Leader B left at 07:00 AM" → "Employee C joined at 10:00 AM"
 *
 * Mỗi dòng work_order_members sinh tối đa 2 sự kiện: JOINED (joined_at) và LEFT
 * (left_at, nếu đã rời). Một nhân viên rời rồi vào lại = dòng member MỚI, nên
 * lịch sử tự nhiên có nhiều cặp JOINED/LEFT cho cùng một người.
 *
 * {@code role} hiện là roleInTask của member; thiết kế để TƯƠNG LAI thêm được
 * sự kiện của leader / chỉ huy trực tiếp / giám sát an toàn (role = "LEADER"...)
 * mà không đổi cấu trúc DTO.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberHistoryEventDTO {

    public enum EventType { JOINED, LEFT }

    private Integer employeeId;
    private String fullName;
    private String role;
    private EventType eventType;
    private LocalDateTime eventTime;
}
