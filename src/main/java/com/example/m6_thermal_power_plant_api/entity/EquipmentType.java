package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;

/**
 * Loại thiết bị (Bơm, Quạt, Van, Động cơ, Máy biến áp...).
 * Table: equipment_types
 *
 * Soft delete: xem {@link BaseSoftDeleteEntity}.
 */
@Entity
@Table(name = "equipment_types")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@ToString(callSuper = true, exclude = "equipmentList")
@EqualsAndHashCode(callSuper = false, of = "id")
public class EquipmentType extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JsonIgnore
    @OneToMany(mappedBy = "equipmentType", fetch = FetchType.LAZY)
    private List<Equipment> equipmentList;
}
