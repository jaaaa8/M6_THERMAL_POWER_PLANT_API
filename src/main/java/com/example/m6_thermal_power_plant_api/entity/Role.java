package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;

/**
 * Vai trò hệ thống (VD: Thủ kho vật tư, Trưởng Ca, Admin...). Phân quyền theo
 * ROLE (không còn permission rời — xem SecurityConfig#roleHierarchy).
 * Table: roles
 *
 * Soft delete: xem {@link BaseSoftDeleteEntity}.
 */
@Entity
@Table(name = "roles")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@ToString(callSuper = true, exclude = "accounts")
@EqualsAndHashCode(callSuper = false, of = "id")
public class Role extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @JsonIgnore
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private List<Account> accounts;
}
