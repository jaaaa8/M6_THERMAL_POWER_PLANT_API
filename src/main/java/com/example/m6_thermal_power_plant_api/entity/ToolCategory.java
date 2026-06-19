package com.example.m6_thermal_power_plant_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import java.util.List;

@Entity
@Table(name = "tool_categories")
@SoftDelete(columnName = "is_deleted", strategy = SoftDeleteType.DELETED)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ToolCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "category_code", unique = true, nullable = false, length = 50)
    private String categoryCode;

    @Column(name = "category_name", nullable = false, length = 255)
    private String categoryName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JsonIgnore
    @OneToMany(mappedBy = "toolCategory", fetch = FetchType.LAZY)
    private List<Tool> tools;
}