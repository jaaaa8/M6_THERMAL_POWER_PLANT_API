package com.example.m6_thermal_power_plant_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import java.util.List;

/**
 * Hệ thống thiết bị (VD: Hệ thống xử lý nước thô).
 * Table: systems
 * Đặt tên EquipmentSystem (không phải "System") để tránh xung đột với java.lang.System.
 *
 * Soft delete: is_deleted do Hibernate quản lý.
 */
@Entity
@Table(name = "systems")
@SoftDelete(columnName = "is_deleted", strategy = SoftDeleteType.DELETED)
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = "equipmentList")
@EqualsAndHashCode(of = "id")
public class EquipmentSystem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JsonIgnore
    @OneToMany(mappedBy = "system", fetch = FetchType.LAZY)
    private List<Equipment> equipmentList;
}
