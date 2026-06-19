package com.example.m6_thermal_power_plant_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Lịch sử thực hiện bảo dưỡng dầu mỡ.
 * Table: lubrication_history
 *
 * Không áp dụng @SoftDelete: là lịch sử thực hiện, không xoá.
 */
@Entity
@Table(name = "lubrication_history")
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class LubricationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private LubricationPlan plan;

    @Column(name = "performed_date")
    private LocalDate performedDate;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
