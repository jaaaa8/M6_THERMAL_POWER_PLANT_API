package com.example.m6_thermal_power_plant_api.dto.employee;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Data
public class EmployeeDTO {
    @NotBlank(message = "Họ và tên không được để trống")
    @Size(min = 2, max = 100, message = "Họ tên phải từ 2 đến 100 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String gmail;

    private String imgPath;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ (VD: 0912345678)")
    private String phone;

    @NotNull(message = "Vui lòng chọn phòng ban")
    @Digits(integer = 10, fraction = 0, message = "Mã phòng ban phải là số")
    private Integer departmentId;

    @NotNull(message = "Vui lòng chọn chuyên môn")
    @Digits(integer = 10, fraction = 0, message = "Mã chuyên môn phải là số")
    private Integer expertiseId;

    @NotNull(message = "Vui lòng chọn chức vụ")
    @Digits(integer = 10, fraction = 0, message = "Mã chức vụ phải là số")
    private Integer positionId;

    private String isActive;
}
