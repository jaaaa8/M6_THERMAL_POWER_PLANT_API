package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.example.m6_thermal_power_plant_api.entity.base.CascadeSoftDelete;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;

/**
 * Thiết bị trong nhà máy.
 * Table: equipment
 *
 * Soft delete: xem {@link BaseSoftDeleteEntity} — thiết bị thanh lý chỉ bị ẩn,
 * lịch sử sửa chữa / bảo dưỡng liên quan vẫn được giữ nguyên trong DB.
 */
@Entity
@Table(name = "equipment")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@ToString(callSuper = true, exclude = {"parameters", "repairRequests", "lubricationPlans"})
@EqualsAndHashCode(callSuper = false, of = "id")
public class Equipment extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Mã KKS - định danh duy nhất cho mỗi thiết bị */
    @Column(name = "kks_code", unique = true, nullable = false, length = 50)
    private String kksCode;

    @Column(nullable = false, length = 255)
    private String name;

    /** EquipmentSystem / EquipmentType đều @SQLRestriction("is_deleted = false")
     *  nên không cần khai báo lại restriction ở 2 quan hệ dưới đây. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "system_id")
    @CascadeSoftDelete
    private EquipmentSystem system;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_type_id")
    @CascadeSoftDelete
    private EquipmentType equipmentType;

    /** Đang vận hành / Đang sửa chữa / Đang hỏng / Đang dự phòng */
    @Column(length = 100)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Đường dẫn file ảnh đính kèm */
    @Column(name = "img_path", columnDefinition = "TEXT")
    private String imgPath;

    @JsonIgnore
    @OneToMany(mappedBy = "equipment", fetch = FetchType.LAZY)
    private List<EquipmentParameter> parameters;

    @JsonIgnore
    @OneToMany(mappedBy = "equipment", fetch = FetchType.LAZY)
    private List<RepairRequest> repairRequests;

    @JsonIgnore
    @OneToMany(mappedBy = "equipment", fetch = FetchType.LAZY)
    private List<LubricationPlan> lubricationPlans;
}
