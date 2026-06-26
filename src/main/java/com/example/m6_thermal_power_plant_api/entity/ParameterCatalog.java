package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;

/**
 * Danh mục tên thông số kỹ thuật (Công suất, Vòng quay, Áp suất...).
 * Table: parameter_catalog
 * Join table parameter_unit_map được quản lý tại đây bằng @ManyToMany.
 *
 * Soft delete: xem {@link BaseSoftDeleteEntity}.
 */
@Entity
@Table(name = "parameter_catalog")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@ToString(callSuper = true, exclude = "units")
@EqualsAndHashCode(callSuper = false, of = "id")
public class ParameterCatalog extends BaseSoftDeleteEntity {

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
    @JsonIgnore
    @OneToMany(mappedBy = "parameter", fetch = FetchType.LAZY)
    private List<EquipmentParameter> equipmentParameters;
}
