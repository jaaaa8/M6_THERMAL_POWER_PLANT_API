package com.example.m6_thermal_power_plant_api.entity;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.example.m6_thermal_power_plant_api.entity.base.CascadeSoftDelete;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Dòng cấp phát thực tế của một phiếu cấp vật tư TIÊU HAO.
 * Table: consumable_exports
 *
 * Mỗi dòng = 1 lần xuất thực tế cho 1 Consumable trong 1 ConsumableIssue,
 * có {@code requested_quantity} (yêu cầu) và {@code actual_quantity} (thực
 * cấp), gắn với equipment_id để biết xuất cho thiết bị nào.
 *
 * MỨC ĐỘ AN TOÀN SOFT-DELETE: ❌ KHÔNG NÊN
 *  - Là dòng cấp phát thực tế đã trừ kho.
 *  - Soft-delete một dòng Export → báo cáo cấp phát thiếu dòng đó NHƯNG sổ
 *    tồn kho (ConsumableInventory) vẫn ghi đã xuất → mất khớp giữa báo cáo
 *    cấp phát và sổ tồn.
 *  - QUY TẮC: tuyệt đối không xoá lẻ một dòng Export. Sai sót xử lý bằng
 *    giao dịch đối ứng trong ConsumableInventory + tạo phiếu nhập trả nếu cần.
 *  - Việc bản ghi xuất hiện ở đây chủ yếu là để cascade theo ConsumableIssue
 *    / Consumable / Equipment khi gốc bị soft-delete (tất cả đều ❌ ở cấp gốc
 *    — coi như không xảy ra trong nghiệp vụ thường ngày).
 */
@Entity
@Table(name = "consumable_exports")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false, of = "id")
public class ConsumableExport extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "export_code", nullable = false, length = 50)
    private String exportCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumable_issue_id", nullable = false)
    @CascadeSoftDelete
    private ConsumableIssue consumableIssue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumable_id", nullable = false)
    @CascadeSoftDelete
    private Consumable consumable;

    @Column(name = "requested_quantity", precision = 10, scale = 2, nullable = false)
    private BigDecimal requestedQuantity;

    @Column(name = "actual_quantity", precision = 10, scale = 2, nullable = false)
    private BigDecimal actualQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    @CascadeSoftDelete
    private Equipment equipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exported_by")
    private Account exportedBy;

    @Column(name = "exported_at")
    private LocalDateTime exportedAt;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(length = 50)
    private String status;
}
