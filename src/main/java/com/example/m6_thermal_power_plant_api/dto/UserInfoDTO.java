package com.example.m6_thermal_power_plant_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Thông tin user hiện tại — dùng cho cả LoginResponse và endpoint GET /me.
 * FE dùng để hiển thị header dashboard và kiểm tra role.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDTO {
    private Integer accountId;
    private String username;
    /** Tên hiển thị (từ employees.full_name). Fallback về username nếu account không gắn employee. */
    private String fullName;
    private List<String> roles;

    // Optional — null nếu account không có employee
    private String employeeCode;
    private String departmentName;
    private String position;
    private String avatarUrl;
}
