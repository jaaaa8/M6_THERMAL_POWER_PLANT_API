package com.example.m6_thermal_power_plant_api.dto.accounts;

import jakarta.validation.constraints.Digits;
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
public class AccountDTO {
    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 8, max = 50, message = "Tên đăng nhập phải từ 8 đến 50 ký tự")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[0-9])[a-z0-9!\"#$%&'()*+,\\-./:;<=>?@\\[\\\\\\]^_`{|}~]+$", message = "Tên đăng nhập phải chứa chữ thường, chữ số và có thể chứa ký tự đặc biệt")
    private String username;

    @jakarta.validation.constraints.Email(message = "Email không đúng định dạng")
    private String email;

    @Digits(integer = 10, fraction = 0, message = "Mã nhân viên phải là số")
    private Integer employeeId;

    @NotNull(message = "Vui lòng chọn vai trò")
    @Size(min = 1, message = "Phải chọn ít nhất một vai trò")
    @com.fasterxml.jackson.annotation.JsonFormat(with = com.fasterxml.jackson.annotation.JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private java.util.List<Integer> roleIds;
}
