package com.example.m6_thermal_power_plant_api.dto.spare_parts;

import com.example.m6_thermal_power_plant_api.entity.SparePartsIssue;
import com.example.m6_thermal_power_plant_api.entity.SparePartsIssueDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Dữ liệu hiển thị một phiếu cấp vật tư thay thế (kèm các dòng chi tiết).
 * KHÔNG nhúng entity (khác với SparePartsIssueRequestDto cũ ở dto/Leader) —
 * {@code from(...)} phải gọi TRONG transaction để nạp các quan hệ LAZY.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SparePartsIssueDTO {

    private Integer id;
    /** Mã của chính phiếu cấp (cột issue_code trên spare_parts_issues). */
    private String issueCode;
    private Integer workOrderId;
    private String orderCode;
    /** Tổng số lượng của mọi dòng chi tiết. */
    private Integer quantity;
    private Integer issuedById;
    private String issuedByName;
    private LocalDateTime issuedAt;
    private List<LineDTO> details;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineDTO {
        private Integer id;
        private Integer sparePartId;
        private String sparePartCode;
        private String sparePartName;
        private String unitName;
        private Integer quantity;

        public static LineDTO from(SparePartsIssueDetail d) {
            return LineDTO.builder()
                    .id(d.getId())
                    .sparePartId(d.getSparePart() != null ? d.getSparePart().getId() : null)
                    .sparePartCode(d.getSparePart() != null ? d.getSparePart().getSparePartCode() : null)
                    .sparePartName(d.getSparePart() != null ? d.getSparePart().getName() : null)
                    .unitName(d.getSparePart() != null && d.getSparePart().getUnit() != null
                            ? d.getSparePart().getUnit().getName() : null)
                    .quantity(d.getQuantity())
                    .build();
        }
    }

    public static SparePartsIssueDTO from(SparePartsIssue issue, List<SparePartsIssueDetail> details) {
        List<LineDTO> lines = details != null ? details.stream().map(LineDTO::from).toList() : List.of();
        SparePartsIssueDTO build = SparePartsIssueDTO.builder()
                .id(issue.getId())
                .issueCode(issue.getIssueCode())
                .workOrderId(issue.getWorkOrder() != null ? issue.getWorkOrder().getId() : null)
                .orderCode(issue.getWorkOrder() != null ? issue.getWorkOrder().getOrderCode() : null)
                .quantity(lines.stream()
                        .map(LineDTO::getQuantity)
                        .filter(Objects::nonNull)
                        .reduce(0, Integer::sum)
                )
                .issuedById(issue.getIssuedBy() != null ? issue.getIssuedBy().getId() : null)
                .issuedByName(issue.getIssuedBy() != null && issue.getIssuedBy().getEmployee() != null
                        ? issue.getIssuedBy().getEmployee().getFullName()
                        : (issue.getIssuedBy() != null ? issue.getIssuedBy().getUsername() : null))
                .issuedAt(issue.getIssuedAt())
                .details(lines)
                .build();
        return build;
    }
}
