package com.example.m6_thermal_power_plant_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import java.util.List;

/**
 * Công cụ dụng cụ (CCDC) trong kho.
 * Table: tools
 *
 * Soft delete: is_deleted do Hibernate quản lý — CCDC hư hỏng/thanh lý chỉ
 * bị ẩn, nhật ký mượn/trả liên quan vẫn được giữ nguyên.
 */
@Entity
@Table(name = "tools")
@SoftDelete(columnName = "is_deleted", strategy = SoftDeleteType.DELETED)
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = "borrowLogs")
@EqualsAndHashCode(of = "id")
public class Tool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tool_code", unique = true, nullable = false, length = 50)
    private String toolCode;

    @Column(nullable = false, length = 255)
    private String name;

    /** Số lượng hiện có trong kho */
    @Builder.Default
    @Column
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
