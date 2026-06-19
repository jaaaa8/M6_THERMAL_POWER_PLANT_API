package com.example.m6_thermal_power_plant_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import java.util.List;

/**
 * Loại thiết bị (Bơm, Quạt, Van, Động cơ, Máy biến áp...).
 * Table: equipment_types
 *
 * Soft delete: is_deleted do Hibernate quản lý.
 */
@Entity
@Table(name = "equipment_types")
@SoftDelete(columnName = "is_deleted", strategy = SoftDeleteType.DELETED)
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = "equipmentList")
@EqualsAndHashCode(of = "id")
public class EquipmentType {

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
