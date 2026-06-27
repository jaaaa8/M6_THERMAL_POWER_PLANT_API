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
 * Dòng cấp phát thực tế của một phiếu cấp vật tư THAY THẾ.
 * Table: spare_part_exports
 *
 * Mỗi dòng = 1 lần xuất thực tế cho 1 SparePart trong 1 SparePartsIssue,
 * có {@code requested_quantity} (yêu cầu) và {@code actual_quantity} (thực
 * cấp), gắn với equipment_id để biết xuất cho thiết bị nào.
 *
 * MỨC ĐỘ AN TOÀN SOFT-DELETE: ❌ KHÔNG NÊN
 *  - Là dòng cấp phát thực tế đã trừ kho.
 *  - Soft-delete một dòng Export → báo cáo cấp phát thiếu dòng đó NHƯNG sổ
 *    tồn kho (SparePartsInventory) vẫn ghi đã xuất → mất khớp giữa báo cáo
 *    cấp phát và sổ tồn.
 *  - QUY TẮC: tuyệt đối không xoá lẻ một dòng Export. Sai sót xử lý bằng
 *    giao dịch đối ứng trong SparePartsInventory + tạo phiếu nhập trả nếu cần.
 *  - Việc bản ghi xuất hiện ở đây chủ yếu là để cascade theo SparePartsIssue
 *    / SparePart / Equipment khi gốc bị soft-delete (tất cả đều ❌ ở cấp gốc
 *    — coi như không xảy ra trong nghiệp vụ thường ngày).
 */
@Entity
@Table(name = "spare_part_exports")
@SQLRestriction("is_deleted = false")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false, of = "id")
public class SparePartExport extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "export_code", nullable = false, length = 50)
    private String exportCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spare_parts_issue_id", nullable = false)
    @CascadeSoftDelete
    private SparePartsIssue sparePartsIssue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spare_part_id", nullable = false)
    @CascadeSoftDelete
    private SparePart sparePart;

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
