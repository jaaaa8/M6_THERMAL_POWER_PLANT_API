package com.example.m6_thermal_power_plant_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Danh mục vật tư tiêu hao (RP7, Dẻ lau, Dầu bôi trơn...).
 * Table: consumable
 *
 * Soft delete: is_deleted do Hibernate quản lý.
 */
@Entity
@Table(name = "consumable")
@SoftDelete(columnName = "is_deleted", strategy = SoftDeleteType.DELETED)
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = {"inventoryTransactions", "issues", "lubricationPlans"})
@EqualsAndHashCode(of = "id")
public class Consumable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "consumable_code", unique = true, nullable = false, length = 30)
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
