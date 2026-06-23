package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.example.m6_thermal_power_plant_api.entity.enums.PartStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.List;

/**
 * Danh mục vật tư tiêu hao (RP7, Dẻ lau, Dầu bôi trơn...).
 * Table: consumable
 *
 * Có 2 cờ trạng thái KHÔNG thay thế nhau:
 * - status     : trạng thái kinh doanh (còn dùng / ngừng dùng loại vật tư này)
 * - is_deleted : xoá mềm hành chính, xem {@link BaseSoftDeleteEntity}
 */
@Entity
@Table(name = "consumable")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@ToString(callSuper = true, exclude = {"inventoryTransactions", "issues", "lubricationPlans"})
@EqualsAndHashCode(callSuper = false, of = "id")
public class Consumable extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Uniqueness enforced by upgrade-soft-delete-unique-constraints.sql (composite UNIQUE with active_flag)
    @Column(name = "consumable_code", nullable = false, length = 30)
    private String consumableCode;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 100)
    private String manufacturer;

    /** Đường dẫn file ảnh đính kèm */
    @Column(name = "img_path", columnDefinition = "TEXT")
    private String imgPath;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private PartStatus status = PartStatus.ACTIVE;

    @JsonIgnore
    @OneToMany(mappedBy = "consumable", fetch = FetchType.LAZY)
    private List<ConsumableInventory> inventoryTransactions;

    @JsonIgnore
    @OneToMany(mappedBy = "consumable", fetch = FetchType.LAZY)
    private List<ConsumableIssue> issues;

    @JsonIgnore
    @OneToMany(mappedBy = "consumable", fetch = FetchType.LAZY)
    private List<LubricationPlan> lubricationPlans;
}
