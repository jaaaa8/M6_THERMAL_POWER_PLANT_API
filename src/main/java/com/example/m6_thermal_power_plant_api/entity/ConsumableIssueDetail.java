package com.example.m6_thermal_power_plant_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "consumable_issue_details")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumableIssueDetail {
    @Id
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "issue_id")
    private ConsumableIssue issue;

    @ManyToOne
    @JoinColumn(name = "consumable_id")
    private Consumable consumable;

    private BigDecimal quantity;
}
