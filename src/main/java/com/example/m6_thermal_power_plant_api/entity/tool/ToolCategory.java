package com.example.m6_thermal_power_plant_api.entity.tool;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;

@Entity
@Table(name = "tool_categories")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = "tools")
@EqualsAndHashCode(callSuper = false, of = "id")
public class ToolCategory extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** TC001, TC002... */
    @Column(name = "category_code", nullable = false, unique = true, length = 50)
    private String categoryCode;

    /** Tháo lắp, Đo điện, Hàn cắt... */
    @Column(name = "category_name", nullable = false, length = 255)
    private String categoryName;

    /** Mô tả nhóm công cụ */
    @Column(columnDefinition = "TEXT")
    private String description;

    @JsonIgnore
    @OneToMany(mappedBy = "toolCategory")
    private List<Tool> tools;
}