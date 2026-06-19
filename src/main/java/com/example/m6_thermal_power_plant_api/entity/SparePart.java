package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.enums.PartStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Danh mục vật tư thay thế (Vòng bi SKF, Van, Dây điện, CB...).
 * Table: spare_parts
 *
 * Soft delete: is_deleted do Hibernate quản lý.
 */
@Entity
@Table(name = "spare_parts")
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = {"inventoryTransactions", "issues"})
@EqualsAndHashCode(of = "id")
public class SparePart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "spare_part_code", unique = true, nullable = false, length = 30)
    private String sparePartCode;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 100)
    private String manufacturer;

    @Column(columnDefinition = "TEXT")
    private PartStatus status = PartStatus.ACTIVE;

    /** Đường dẫn file ảnh đính kèm */
    @Column(name = "img_path", columnDefinition = "TEXT")
    private String imgPath;

    @JsonIgnore
    @OneToMany(mappedBy = "sparePart", fetch = FetchType.LAZY)
    private List<SparePartsInventory> inventoryTransactions;

    @JsonIgnore
    @OneToMany(mappedBy = "sparePart", fetch = FetchType.LAZY)
    private List<SparePartsIssue> issues;
}
