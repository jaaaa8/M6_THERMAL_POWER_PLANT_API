package com.example.m6_thermal_power_plant_api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import java.util.List;

/**
 * Danh mục tên thông số kỹ thuật (Công suất, Vòng quay, Áp suất...).
 * Table: parameter_catalog
 * Join table parameter_unit_map được quản lý tại đây bằng @ManyToMany.
 *
 * Soft delete: is_deleted do Hibernate quản lý.
 */
@Entity
@Table(name = "parameter_catalog")
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = "units")
@EqualsAndHashCode(of = "id")
public class ParameterCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Các đơn vị đo có thể áp dụng cho thông số này.
     * Join table: parameter_unit_map (parameter_id, unit_id)
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "parameter_unit_map",
        joinColumns        = @JoinColumn(name = "parameter_id"),
        inverseJoinColumns = @JoinColumn(name = "unit_id")
    )
    private List<Unit> units;
}
