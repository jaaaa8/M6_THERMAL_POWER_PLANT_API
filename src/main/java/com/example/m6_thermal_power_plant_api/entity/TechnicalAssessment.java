package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.enums.TechnicalAssessmentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Biên bản đánh giá kỹ thuật — Tổ trưởng lập khi thiết bị cần vật tư thay thế.
 * Table: technical_assessments
 *
 * Không áp dụng @SoftDelete: là biên bản đã ký các bên, không xoá.
 */
@Entity
@Table(name = "technical_assessments")
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class TechnicalAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "technical_code", unique = true, nullable = false, length = 50)
    private String technicalCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id")
    private WorkOrder workOrder;

    /** Tổ trưởng thực hiện đánh giá (đăng nhập bằng tài khoản) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assessor_id")
    private Account assessor;

    @Column(columnDefinition = "TEXT")
    private String result;

    /** Đường dẫn file biên bản PDF đã ký */
    @Column(name = "attachment_path", length = 500)
    private String attachmentPath;

    /** Đường dẫn file ảnh đính kèm */
    @Column(name = "img_path", columnDefinition = "TEXT")
    private String imgPath;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TEXT")
    private TechnicalAssessmentStatus status = TechnicalAssessmentStatus.PENDING;
}
