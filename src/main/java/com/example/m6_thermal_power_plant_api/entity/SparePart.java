package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.example.m6_thermal_power_plant_api.entity.enums.PartStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.List;

/**
 * Danh mục vật tư thay thế (Vòng bi SKF, Van, Dây điện, CB...).
 * Table: spare_parts
 *
 * Có 2 cờ trạng thái KHÔNG thay thế nhau:
 * - status     : trạng thái kinh doanh (còn dùng / ngừng dùng loại vật tư này)
 * - is_deleted : xoá mềm hành chính, xem {@link BaseSoftDeleteEntity}
 */
@Entity
@Table(name = "spare_parts")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@ToString(callSuper = true, exclude = {"inventoryTransactions", "issues", "receipts", "exports"})
@EqualsAndHashCode(callSuper = false, of = "id")
public class SparePart extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // composite voi cot active_flag de tao unique sau khi run sql script o thu muc db
    @Column(name = "spare_part_code", nullable = false, length = 30)
    private String sparePartCode;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 100)
    private String manufacturer;

    /** Đường dẫn file ảnh đính kèm */
    @Lob
    @Column(name = "img_path", columnDefinition = "LONGTEXT")
    private String imgPath;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private PartStatus status = PartStatus.ACTIVE;

    @JsonIgnore
    @OneToMany(mappedBy = "sparePart", fetch = FetchType.LAZY)
    private List<SparePartsInventory> inventoryTransactions;

    @JsonIgnore
    @OneToMany(mappedBy = "sparePart", fetch = FetchType.LAZY)
    private List<SparePartsIssueDetail> issueDetails;

    @JsonIgnore
    @OneToMany(mappedBy = "sparePart", fetch = FetchType.LAZY)
    private List<SparePartReceipt> receipts;

    @JsonIgnore
    @OneToMany(mappedBy = "sparePart", fetch = FetchType.LAZY)
    private List<SparePartExport> exports;
}
