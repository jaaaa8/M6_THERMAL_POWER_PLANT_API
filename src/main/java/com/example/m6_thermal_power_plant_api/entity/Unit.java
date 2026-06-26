package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;

/**
 * Đơn vị đo lường (KW, bar, m3/h, V, A, vòng/phút...).
 * Table: units
 *
 * Không soft-delete: bảng lookup nhỏ, ít thay đổi, không phải nơi xảy ra
 * nghiệp vụ "xoá" theo nghĩa cần giữ lại lịch sử.
 */
@Entity
@Table(name = "units")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@ToString(callSuper = true, exclude = "parameters")
@EqualsAndHashCode(callSuper = false, of = "id")
public class Unit extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JsonIgnore
    @ManyToMany(mappedBy = "units", fetch = FetchType.LAZY)
    private List<ParameterCatalog> parameters;
}
