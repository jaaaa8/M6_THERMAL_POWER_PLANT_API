package com.example.m6_thermal_power_plant_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import java.util.List;

/**
 * Thiết bị trong nhà máy.
 * Table: equipment
 *
 * Soft delete: is_deleted do Hibernate quản lý — thiết bị thanh lý chỉ bị ẩn,
 * lịch sử sửa chữa / bảo dưỡng liên quan vẫn được giữ nguyên trong DB.
 */
@Entity
@Table(name = "equipment")
@SoftDelete(columnName = "is_deleted", strategy = SoftDeleteType.DELETED)
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = {"parameters", "repairRequests", "lubricationPlans"})
@EqualsAndHashCode(of = "id")
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Mã KKS - định danh duy nhất cho mỗi thiết bị */
    @Column(name = "kks_code", unique = true, nullable = false, length = 50)
    private String kksCode;

    @Column(nullable = false, length = 255)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "system_id")
    private EquipmentSystem system;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "equipment_type_id")
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
