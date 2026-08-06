package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.PasswordOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordOtpRepository extends JpaRepository<PasswordOtp, Integer> {

    /**
     * Mã CHƯA DÙNG mới nhất của một tài khoản. Một truy vấn phục vụ cả hai việc:
     * xác thực mã người dùng nhập, và chặn spam gửi lại (xem thời điểm tạo).
     *
     * Chỉ lấy mã mới nhất là có chủ ý: xin mã mới thì mã cũ coi như bỏ, người
     * dùng luôn dùng mã trong email gần nhất.
     */
    Optional<PasswordOtp> findFirstByAccount_IdAndConsumedAtIsNullOrderByCreatedAtDesc(Integer accountId);
}
