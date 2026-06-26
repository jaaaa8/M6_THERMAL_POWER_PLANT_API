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
@Table(name = "spare_parts_issue_details")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SparePartsIssueDetail {
    @Id
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "issue_id")
    private SparePartsIssue issue;

    @ManyToOne
    @JoinColumn(name = "spare_part_id")
    private SparePart sparePart;

    private BigDecimal quantity;
}
