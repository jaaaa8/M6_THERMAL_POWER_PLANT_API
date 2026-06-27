package com.example.m6_thermal_power_plant_api.entity.tool;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;

@Entity
@Table(name = "tools")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"borrowLogs", "toolCategory"})
@EqualsAndHashCode(callSuper = false, of = "id")
public class Tool extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** MCCDC-0001 */
    @Column(name = "tool_code", nullable = false, unique = true, length = 50)
    private String toolCode;

    /** Bộ cờ lê 12 món */
    @Column(nullable = false, length = 255)
    private String name;

    /** Chủng loại */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tool_category_id", nullable = false)
    private ToolCategory toolCategory;

    /** Bộ, Cái, Chiếc, Đôi... */
    @Column(nullable = false, length = 50)
    private String unit;

    /** Tổng số lượng trong kho (bao gồm cả đang mượn và hư hỏng) */
    @Builder.Default
    @Column(nullable = false)
    private Integer quantity = 0;

    /** Số lượng đang được mượn, chưa trả */
    @Builder.Default
    @Column(name = "quantity_borrowed", nullable = false)
    private Integer quantityBorrowed = 0;

    /** Số lượng đã hư hỏng / loại khỏi sử dụng, không tính vào số khả dụng */
    @Builder.Default
    @Column(name = "quantity_damaged", nullable = false)
    private Integer quantityDamaged = 0;

    /** Ghi chú riêng của công cụ */
    @Column(columnDefinition = "TEXT")
    private String note;

    @JsonIgnore
    @OneToMany(mappedBy = "tool")
    private List<ToolBorrowLog> borrowLogs;

    /** Số lượng còn khả dụng để cho mượn — không lưu DB, tính từ 3 trường trên */
    public Integer getQuantityAvailable() {
        return quantity - quantityBorrowed - quantityDamaged;
    }
}