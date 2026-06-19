package com.example.m6_thermal_power_plant_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Đơn vị đo lường (KW, bar, m3/h, V, A, vòng/phút...).
 * Table: units
 *
 * Không áp dụng @SoftDelete: bảng lookup nhỏ, ít thay đổi, không phải nơi
 * xảy ra nghiệp vụ "xoá" theo nghĩa cần giữ lại lịch sử.
 */
@Entity
@Table(name = "units")
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = "parameters")
@EqualsAndHashCode(of = "id")
public class Unit {

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
