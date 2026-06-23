package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.example.m6_thermal_power_plant_api.entity.base.CascadeSoftDelete;
import com.example.m6_thermal_power_plant_api.entity.enums.TechnicalAssessmentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * Biên bản đánh giá kỹ thuật — Tổ trưởng lập khi thiết bị cần vật tư thay thế.
 * Table: technical_assessments
 *
 * Không soft-delete: là biên bản đã ký các bên, không xoá — dùng field
 * {@code status} (PENDING/IN_PROGRESS/COMPLETED/REJECTED) để theo dõi tiến
 * trình xử lý thay vì ẩn bản ghi. Account đã @SQLRestriction nên không cần
 * khai báo lại restriction ở quan hệ assessor.
 */
@Entity
@Table(name = "technical_assessments")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "id")
public class TechnicalAssessment extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "technical_code", unique = true, nullable = false, length = 50)
    private String technicalCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id")
    @CascadeSoftDelete
    private WorkOrder workOrder;

    /** Tổ trưởng thực hiện đánh giá (đăng nhập bằng tài khoản) */
    @ManyToOne(fetch = FetchType.LAZY)
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

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private TechnicalAssessmentStatus status = TechnicalAssessmentStatus.PENDING;
}
