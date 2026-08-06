package com.example.m6_thermal_power_plant_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Mã OTP dùng một lần để xác nhận đổi mật khẩu (V27).
 *
 * KHÁC quy ước chung của dự án: entity này KHÔNG kế thừa
 * {@code BaseSoftDeleteEntity}. Đây là dữ liệu tạm sống vài phút — xoá mềm một
 * mã OTP là vô nghĩa, và thêm @SQLRestriction chỉ làm truy vấn nặng thêm.
 *
 * {@code otpHash} là BCrypt của mã, không phải mã thô.
 */
@Entity
@Table(name = "password_otps")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "otp_hash", nullable = false)
    private String otpHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** Số lần nhập SAI. Chặn dò cạn mã 6 chữ số. */
    @Column(nullable = false)
    @Builder.Default
    private Integer attempts = 0;

    /** Khác null = mã đã dùng rồi, không dùng lại được. */
    @Column(name = "consumed_at")
    private LocalDateTime consumedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
