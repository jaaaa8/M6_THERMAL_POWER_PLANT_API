package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;

/**
 * Chuyên môn.
 * Table: expertises
 */
@Entity
@Table(name = "expertises")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@ToString(callSuper = true, exclude = "employees")
@EqualsAndHashCode(callSuper = false, of = "id")
public class Expertise extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "expertise_code", nullable = false, length = 50)
    private String expertiseCode;

    @Column(nullable = false, length = 255)
    private String name;

    @JsonIgnore
    @OneToMany(mappedBy = "expertise", fetch = FetchType.LAZY)
    private List<Employee> employees;
}
