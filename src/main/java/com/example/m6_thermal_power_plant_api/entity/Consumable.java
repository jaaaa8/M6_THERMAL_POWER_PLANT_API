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
 * Danh mục vật tư tiêu hao (RP7, Dẻ lau, Dầu bôi trơn...).
 * Table: consumable
 *
 * Có 2 cờ trạng thái KHÔNG thay thế nhau:
 *  - status (PartStatus): trạng thái kinh doanh — còn dùng / ngừng dùng loại
 *    vật tư này. Dùng cờ này khi muốn "khoá danh mục" mà vẫn giữ lịch sử.
 *  - is_deleted: xoá mềm hành chính, xem {@link BaseSoftDeleteEntity}.
 *
 * MỨC ĐỘ AN TOÀN SOFT-DELETE: ❌ RỦI RO CAO
 *  - Soft-delete kéo theo TOÀN BỘ:
 *    + ConsumableInventory (sổ nhập/xuất loại vật tư này) → mất khả năng
 *      truy vết tồn kho.
 *    + ConsumableIssue → ConsumableIssueDetail, ConsumableExport (chứng từ
 *      cấp phát đã ký).
 *    + LubricationPlan (kế hoạch bôi trơn dùng loại dầu này).
 *  - Hậu quả: lịch sử kho và sử dụng vật tư biến mất khỏi UI.
 *  - KHUYẾN NGHỊ: nếu chỉ muốn "ngừng dùng loại vật tư này" (không nhập
 *    thêm, không cấp thêm) → đổi cờ {@code status} (đã có sẵn cho đúng mục
 *    đích này), KHÔNG soft-delete.
 *  - Soft-delete chỉ làm khi loại vật tư mới nhập danh mục nhầm và CHƯA
 *    TỪNG có giao dịch nào (Inventory rỗng, Issue rỗng, Plan rỗng) — service
 *    nên check trước khi cho phép.
 */
@Entity
@Table(name = "consumable")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@ToString(callSuper = true, exclude = {"inventoryTransactions", "issueDetails", "lubricationPlans", "receipts", "exports"})
@EqualsAndHashCode(callSuper = false, of = "id")
public class Consumable extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // composite voi cot active_flag de tao unique sau khi run sql script o thu muc db
    @Column(name = "consumable_code", nullable = false, length = 30)
    private String consumableCode;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 100)
    private String manufacturer;

    /** Đường dẫn file ảnh đính kèm */
    @Column(name = "img_path", columnDefinition = "TEXT")
    private String imgPath;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private PartStatus status = PartStatus.ACTIVE;

    @JsonIgnore
    @OneToMany(mappedBy = "consumable", fetch = FetchType.LAZY)
    private List<ConsumableInventory> inventoryTransactions;

    @JsonIgnore
    @OneToMany(mappedBy = "consumable")
    private List<ConsumableIssueDetail> issueDetails;

    @JsonIgnore
    @OneToMany(mappedBy = "consumable", fetch = FetchType.LAZY)
    private List<LubricationPlan> lubricationPlans;

    @JsonIgnore
    @OneToMany(mappedBy = "consumable", fetch = FetchType.LAZY)
    private List<ConsumableReceipt> receipts;

    @JsonIgnore
    @OneToMany(mappedBy = "consumable", fetch = FetchType.LAZY)
    private List<ConsumableExport> exports;
}
