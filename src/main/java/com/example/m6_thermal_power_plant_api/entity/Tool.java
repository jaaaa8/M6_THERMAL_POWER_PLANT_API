package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;

/**
 * Công cụ dụng cụ (CCDC) trong kho.
 * Table: tools
 *
 * Soft delete: xem {@link BaseSoftDeleteEntity} — CCDC hư hỏng/thanh lý chỉ
 * bị ẩn, nhật ký mượn/trả liên quan vẫn được giữ nguyên.
 */
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

    @Column(name = "tool_code", unique = true, nullable = false, length = 50)
    private String toolCode;

    @Column(nullable = false, length = 255)
    private String name;

    /** Chủng loại công cụ. ToolCategory đã @SQLRestriction nên không cần
     *  khai báo lại restriction ở đây. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tool_category_id")
    private ToolCategory toolCategory;

    /** Số lượng hiện có trong kho */
    @Builder.Default
    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Đường dẫn file ảnh đính kèm */
    @Column(name = "img_path", columnDefinition = "TEXT")
    private String imgPath;

    @JsonIgnore
    @OneToMany(mappedBy = "tool", fetch = FetchType.LAZY)
    private List<ToolBorrowLog> borrowLogs;
}
