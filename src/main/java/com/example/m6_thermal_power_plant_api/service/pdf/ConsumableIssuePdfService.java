package com.example.m6_thermal_power_plant_api.service.pdf;

import com.example.m6_thermal_power_plant_api.dto.file.FileUploadResult;
import com.example.m6_thermal_power_plant_api.entity.Consumable;
import com.example.m6_thermal_power_plant_api.entity.ConsumableIssue;
import com.example.m6_thermal_power_plant_api.entity.ConsumableIssueDetail;
import com.example.m6_thermal_power_plant_api.entity.Employee;
import com.example.m6_thermal_power_plant_api.entity.WorkOrder;
import com.example.m6_thermal_power_plant_api.exception.ObjectNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.IConsumableIssueDetailRepository;
import com.example.m6_thermal_power_plant_api.repository.IConsumableIssueRepository;
import com.example.m6_thermal_power_plant_api.service.util.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sinh bản in PDF "Phiếu đề nghị cấp phát vật tư, trang thiết bị" cho một phiếu
 * cấp vật tư tiêu hao (template {@code pdf/issue.html}).
 *
 * Khác {@link WorkOrderPdfService}: phiếu cấp vật tư là BẤT BIẾN sau khi tạo
 * (cấp nhầm thì lập phiếu đảo chiều, không sửa phiếu cũ — xem javadoc
 * {@link ConsumableIssue}). Vì vậy KHÔNG cần cơ chế "đóng băng bản lưu" như PCT:
 * mỗi lần xuất chỉ render lại đúng nội dung cũ rồi upload đè cùng public_id, URL
 * trong pdf_path không đổi.
 *
 * Người ĐỀ NGHỊ in trên phiếu là NGƯỜI LÃNH ĐẠO công việc của PCT (đội cần vật
 * tư), KHÔNG phải {@code issuedBy} (tài khoản thao tác cấp phát). Hai mục hệ
 * thống không có dữ liệu — "Kính gửi" (đơn vị cấp phát) và "Lý do đề nghị" —
 * được in dòng chấm để điền tay, giống cách làm ở bản in PCT.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumableIssuePdfService {

    /** Thư mục Cloudinary chứa PDF phiếu cấp vật tư tiêu hao. */
    static final String CLOUDINARY_FOLDER = "consumable-issues";
    /** Số dòng tối thiểu của bảng vật tư (bù dòng trống giữ chỗ như mẫu giấy). */
    static final int MIN_ITEM_ROWS = 5;

    private static final String DOTS = "..........";

    private final IConsumableIssueRepository issueRepository;
    private final IConsumableIssueDetailRepository detailRepository;
    private final PDFService pdfService;
    private final FileUploadService fileUploadService;

    /** Kết quả render: nội dung PDF + URL Cloudinary (null nếu upload lỗi). */
    public record ConsumableIssuePdf(String issueCode, byte[] content, String url) {
    }

    /**
     * Render PDF phiếu cấp vật tư cho việc XUẤT/IN, đồng thời upload đè bản
     * Cloudinary cũ và lưu pdf_path. Upload lỗi KHÔNG làm hỏng việc tải phiếu —
     * vẫn trả về nội dung PDF, chỉ log cảnh báo và giữ pdf_path cũ.
     */
    @Transactional
    public ConsumableIssuePdf render(Integer workOrderId, Integer issueId) {
        ConsumableIssue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Khong tim thay phieu cap vat tu voi id: " + issueId));

        // Resource lồng nhau: chặn in phiếu của PCT khác qua đường dẫn sai.
        Integer ownerId = issue.getWorkOrder() != null ? issue.getWorkOrder().getId() : null;
        if (!java.util.Objects.equals(ownerId, workOrderId)) {
            throw new ObjectNotFoundException("Phieu cap vat tu id " + issueId
                    + " khong thuoc phieu cong tac id " + workOrderId + ".");
        }

        List<ConsumableIssueDetail> details = detailRepository.findByIssue_Id(issue.getId());
        byte[] pdf = pdfService.renderPdf("pdf/issue", buildModel(issue, details));

        String url = null;
        try {
            FileUploadResult uploaded =
                    fileUploadService.uploadPdf(pdf, CLOUDINARY_FOLDER, issue.getConsumableCode());
            issue.setPdfPath(uploaded.secureUrl());
            issueRepository.save(issue);
            url = uploaded.secureUrl();
        } catch (IOException e) {
            log.warn("Upload PDF phieu cap vat tu {} len Cloudinary that bai — van tra ve noi dung PDF.",
                    issue.getConsumableCode(), e);
        }
        return new ConsumableIssuePdf(issue.getConsumableCode(), pdf, url);
    }

    private Map<String, Object> buildModel(ConsumableIssue issue, List<ConsumableIssueDetail> details) {
        Map<String, Object> model = new HashMap<>();

        LocalDateTime issuedAt = issue.getIssuedAt();
        model.put("issuedDay", issuedAt != null ? String.format("%02d", issuedAt.getDayOfMonth()) : "......");
        model.put("issuedMonth", issuedAt != null ? String.format("%02d", issuedAt.getMonthValue()) : "......");
        model.put("issuedYear", issuedAt != null ? String.valueOf(issuedAt.getYear()) : "......");

        // Người đề nghị = người lãnh đạo công việc của PCT (đội cần vật tư).
        WorkOrder workOrder = issue.getWorkOrder();
        Employee leader = workOrder != null ? workOrder.getLeader() : null;
        model.put("requesterName", leader != null && leader.getFullName() != null
                ? leader.getFullName() : DOTS);
        model.put("requesterPosition", leader != null && leader.getPosition() != null
                ? leader.getPosition().getName() : DOTS);
        model.put("requesterDepartment", leader != null && leader.getDepartment() != null
                ? leader.getDepartment().getName() : DOTS);

        // Không có trong hệ thống — điền tay trên bản giấy.
        model.put("recipientDepartment", DOTS);
        model.put("reason", DOTS);

        model.put("orderCode", workOrder != null && workOrder.getOrderCode() != null
                ? workOrder.getOrderCode() : DOTS);
        model.put("description", workOrder != null && workOrder.getRepairDescription() != null
                ? workOrder.getRepairDescription() : DOTS);

        List<Map<String, String>> itemRows = new ArrayList<>();
        for (ConsumableIssueDetail detail : details) {
            Consumable consumable = detail.getConsumable();
            Map<String, String> row = new HashMap<>();
            row.put("name", consumable != null && consumable.getName() != null ? consumable.getName() : "");
            row.put("code", consumable != null && consumable.getConsumableCode() != null
                    ? consumable.getConsumableCode() : "");
            row.put("unit", consumable != null && consumable.getUnit() != null
                    && consumable.getUnit().getName() != null ? consumable.getUnit().getName() : "");
            row.put("quantity", formatQuantity(detail.getQuantity()));
            itemRows.add(row);
        }
        // Dòng đệm giữ chỗ như mẫu giấy — template bỏ trống cả cột STT khi name rỗng.
        while (itemRows.size() < MIN_ITEM_ROWS) {
            itemRows.add(Map.of("name", "", "code", "", "unit", "", "quantity", ""));
        }
        model.put("itemRows", itemRows);

        return model;
    }

    /** "2.00" -> "2", "1.50" -> "1.5" — bỏ số 0 thừa cho gọn bản in. */
    private static String formatQuantity(BigDecimal quantity) {
        return quantity != null ? quantity.stripTrailingZeros().toPlainString() : "";
    }
}
