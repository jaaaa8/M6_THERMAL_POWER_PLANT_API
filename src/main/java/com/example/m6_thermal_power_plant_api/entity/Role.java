package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;
import java.util.Set;

/**
 * Vai trò / Quyền hệ thống (VD: Thủ kho vật tư, Trưởng Ca, Admin...).
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
@ToString(callSuper = true, exclude = {"accounts", "permissions"})
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

    /**
     * Dùng Set (KHÔNG dùng List): để @EntityGraph fetch roles + roles.permissions
     * cùng lúc mà không dính MultipleBagFetchException. Xem {@link Account#roles}.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;
}
