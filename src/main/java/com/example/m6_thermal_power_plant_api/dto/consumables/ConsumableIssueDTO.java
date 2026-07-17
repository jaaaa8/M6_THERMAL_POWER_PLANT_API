package com.example.m6_thermal_power_plant_api.dto.consumables;

import com.example.m6_thermal_power_plant_api.entity.ConsumableIssue;
import com.example.m6_thermal_power_plant_api.entity.ConsumableIssueDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Dữ liệu hiển thị một phiếu cấp vật tư tiêu hao (kèm các dòng chi tiết).
 * {@code from(...)} phải gọi TRONG transaction để nạp các quan hệ LAZY.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumableIssueDTO {

    private Integer id;
    /** Mã của chính phiếu cấp (cột consumable_code trên consumable_issues). */
    private String issueCode;
    private Integer workOrderId;
    private String orderCode;
    private String transactionType;
    /** Tổng số lượng của mọi dòng chi tiết. */
    private BigDecimal quantity;
    private Integer issuedById;
    private String issuedByName;
    private LocalDateTime issuedAt;
    private String status;
    private String attachmentPath;
    private List<LineDTO> details;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineDTO {
        private Integer id;
        private Integer consumableId;
        private String consumableCode;
        private String consumableName;
        private String unitName;
        private BigDecimal quantity;
        private BigDecimal currentStock;

        public static LineDTO from(ConsumableIssueDetail d) {
            return LineDTO.builder()
                    .id(d.getId())
                    .consumableId(d.getConsumable() != null ? d.getConsumable().getId() : null)
                    .consumableCode(d.getConsumable() != null ? d.getConsumable().getConsumableCode() : null)
                    .consumableName(d.getConsumable() != null ? d.getConsumable().getName() : null)
                    .unitName(d.getConsumable() != null && d.getConsumable().getUnit() != null
                            ? d.getConsumable().getUnit().getName() : null)
                    .quantity(d.getQuantity())
                    .build();
        }
    }

    public static ConsumableIssueDTO from(ConsumableIssue issue, List<ConsumableIssueDetail> details) {
        return ConsumableIssueDTO.builder()
                .id(issue.getId())
                .issueCode(issue.getConsumableCode())
                .workOrderId(issue.getWorkOrder() != null ? issue.getWorkOrder().getId() : null)
                .orderCode(issue.getWorkOrder() != null ? issue.getWorkOrder().getOrderCode() : null)
                .transactionType(issue.getTransactionType())
                .quantity(issue.getQuantity())
                .issuedById(issue.getIssuedBy() != null ? issue.getIssuedBy().getId() : null)
                .issuedByName(issue.getIssuedBy() != null && issue.getIssuedBy().getEmployee() != null
                        ? issue.getIssuedBy().getEmployee().getFullName()
                        : (issue.getIssuedBy() != null ? issue.getIssuedBy().getUsername() : null))
                .issuedAt(issue.getIssuedAt())
                .status(issue.getStatus() != null ? issue.getStatus().name() : null)
                .attachmentPath(issue.getAttachmentPath())
                .details(details != null ? details.stream().map(LineDTO::from).toList() : List.of())
                .build();
    }
}
