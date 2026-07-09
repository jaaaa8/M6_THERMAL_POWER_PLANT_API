package com.example.m6_thermal_power_plant_api.service.pdf;

import com.example.m6_thermal_power_plant_api.dto.consumables.ConsumableIssueDTO;
import com.example.m6_thermal_power_plant_api.dto.file.FileUploadResult;
import com.example.m6_thermal_power_plant_api.dto.spare_parts.SparePartsIssueDTO;
import com.example.m6_thermal_power_plant_api.dto.supplies_issue.SuppliesIssueBatchDTO;
import com.example.m6_thermal_power_plant_api.dto.supplies_issue.SuppliesIssueHistoryDTO;
import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.WorkOrder;
import com.example.m6_thermal_power_plant_api.exception.ObjectNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.WorkOrderRepository;
import com.example.m6_thermal_power_plant_api.service.supplies_issue.ISuppliesIssueService;
import com.example.m6_thermal_power_plant_api.service.util.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sinh bản in PDF "Phiếu đề nghị cấp phát vật tư" (templates/pdf/issue.html)
 * cho MỘT phiếu công tác: gom TẤT CẢ dòng vật tư đã cấp (thay thế + tiêu hao,
 * qua mọi lần cấp) vào một bảng duy nhất — 1 PCT in ra 1 phiếu đề nghị.
 *
 * Vòng đời file: phiếu còn sống → {@link #render} luôn trả snapshot mới, KHÔNG
 * upload (dữ liệu cấp phát còn thay đổi, URL cache sẽ cũ). Khi phiếu kết thúc
 * (COMPLETED/CANCELLED) → {@link #archive} render + upload MỘT lần và lưu
 * work_orders.supplies_pdf_path làm bản lưu đóng băng.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SuppliesIssuePdfService {

    /** Số dòng tối thiểu của bảng vật tư — giữ chỗ giống mẫu giấy. */
    private static final int MIN_ITEM_ROWS = 5;

    private static final String CLOUDINARY_FOLDER = "supplies-issues";
    private static final String DOTS = "..........";

    private final WorkOrderRepository workOrderRepository;
    private final ISuppliesIssueService suppliesIssueService;
    private final PDFService pdfService;
    private final FileUploadService fileUploadService;

    /** Nội dung PDF + mã PCT (đặt tên file tải về). */
    public record SuppliesIssuePdf(String orderCode, byte[] content) {
    }

    @Transactional(readOnly = true)
    public SuppliesIssuePdf render(Integer workOrderId) {
        WorkOrder workOrder = loadWorkOrder(workOrderId);
        return new SuppliesIssuePdf(workOrder.getOrderCode(), renderBytes(workOrder));
    }

    /**
     * Bản in của MỘT LẦN cấp vật tư (dòng supplies_issues) — chỉ gồm các dòng vật
     * tư của đúng lần đó, ngày trên phiếu = ngày cấp của lần đó. Lần cấp là dữ
     * liệu bất biến sau khi tạo nên client có thể cache kết quả theo id.
     */
    @Transactional(readOnly = true)
    public SuppliesIssuePdf renderInstance(Integer workOrderId, Integer suppliesIssueId) {
        WorkOrder workOrder = loadWorkOrder(workOrderId);
        SuppliesIssueBatchDTO batch = suppliesIssueService.getByWorkOrder(workOrderId).getIssues().stream()
                .filter(b -> suppliesIssueId.equals(b.getId()))
                .findFirst()
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Khong tim thay lan cap vat tu id " + suppliesIssueId
                                + " thuoc phieu cong tac (" + workOrder.getOrderCode() + ")."));

        List<Map<String, String>> itemRows = new ArrayList<>();
        appendSparePartsRows(itemRows, batch.getSparePartsIssue());
        appendConsumableRows(itemRows, batch.getConsumableIssue());
        while (itemRows.size() < MIN_ITEM_ROWS) {
            itemRows.add(Map.of("name", "", "code", "", "unit", "", "quantity", ""));
        }

        LocalDate issuedDate = batch.getIssuedAt() != null ? batch.getIssuedAt().toLocalDate() : LocalDate.now();
        byte[] pdf = pdfService.renderPdf("pdf/issue", buildModel(workOrder, itemRows, issuedDate));
        return new SuppliesIssuePdf(workOrder.getOrderCode() + "-lan-" + batch.getSeq(), pdf);
    }

    /**
     * ĐÓNG BĂNG bản lưu cuối cùng khi phiếu về trạng thái kết thúc: render +
     * upload + lưu supplies_pdf_path. PCT chưa từng được cấp vật tư thì bỏ qua
     * lặng lẽ (không có gì để lưu); upload lỗi chỉ log cảnh báo — việc đóng
     * phiếu không được phụ thuộc mạng/Cloudinary.
     */
    @Transactional
    public void archive(Integer workOrderId) {
        WorkOrder workOrder = loadWorkOrder(workOrderId);
        byte[] pdf;
        try {
            pdf = renderBytes(workOrder);
        } catch (IllegalStateException e) {
            log.info("PCT {} khong co phieu cap vat tu nao — bo qua luu ban dong bang.",
                    workOrder.getOrderCode());
            return;
        }
        try {
            FileUploadResult uploaded = fileUploadService.uploadPdf(
                    pdf, CLOUDINARY_FOLDER, "vat-tu-" + workOrder.getOrderCode());
            workOrder.setSuppliesPdfPath(uploaded.secureUrl());
            workOrderRepository.save(workOrder);
        } catch (IOException e) {
            log.warn("Upload PDF phieu cap vat tu cua PCT {} len Cloudinary that bai — giu supplies_pdf_path cu.",
                    workOrder.getOrderCode(), e);
        }
    }

    private WorkOrder loadWorkOrder(Integer workOrderId) {
        return workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Khong tim thay phieu cong tac voi id: " + workOrderId));
    }

    private byte[] renderBytes(WorkOrder workOrder) {
        SuppliesIssueHistoryDTO history = suppliesIssueService.getByWorkOrder(workOrder.getId());
        List<Map<String, String>> itemRows = buildItemRows(history);
        if (itemRows.isEmpty()) {
            throw new IllegalStateException(
                    "Phieu cong tac (" + workOrder.getOrderCode()
                            + ") chua duoc cap vat tu lan nao — khong co gi de in.");
        }
        while (itemRows.size() < MIN_ITEM_ROWS) {
            itemRows.add(Map.of("name", "", "code", "", "unit", "", "quantity", ""));
        }
        return pdfService.renderPdf("pdf/issue", buildModel(workOrder, itemRows, LocalDate.now()));
    }

    /** Gộp mọi dòng chi tiết: vật tư thay thế trước, vật tư tiêu hao sau. */
    private static List<Map<String, String>> buildItemRows(SuppliesIssueHistoryDTO history) {
        List<Map<String, String>> rows = new ArrayList<>();
        if (history.getSparePartsIssues() != null) {
            for (SparePartsIssueDTO issue : history.getSparePartsIssues()) {
                appendSparePartsRows(rows, issue);
            }
        }
        if (history.getConsumableIssues() != null) {
            for (ConsumableIssueDTO issue : history.getConsumableIssues()) {
                appendConsumableRows(rows, issue);
            }
        }
        return rows;
    }

    private static void appendSparePartsRows(List<Map<String, String>> rows, SparePartsIssueDTO issue) {
        if (issue == null || issue.getDetails() == null) {
            return;
        }
        for (SparePartsIssueDTO.LineDTO line : issue.getDetails()) {
            rows.add(itemRow(line.getSparePartName(), line.getSparePartCode(),
                    line.getUnitName(), line.getQuantity()));
        }
    }

    private static void appendConsumableRows(List<Map<String, String>> rows, ConsumableIssueDTO issue) {
        if (issue == null || issue.getDetails() == null) {
            return;
        }
        for (ConsumableIssueDTO.LineDTO line : issue.getDetails()) {
            rows.add(itemRow(line.getConsumableName(), line.getConsumableCode(),
                    line.getUnitName(), line.getQuantity()));
        }
    }

    private static Map<String, String> itemRow(String name, String code, String unit, Object quantity) {
        Map<String, String> row = new HashMap<>();
        row.put("name", name != null ? name : "");
        row.put("code", code != null ? code : "");
        row.put("unit", unit != null ? unit : "");
        row.put("quantity", quantity != null ? quantity.toString() : "");
        return row;
    }

    private Map<String, Object> buildModel(WorkOrder workOrder, List<Map<String, String>> itemRows,
                                           LocalDate issuedDate) {
        Map<String, Object> model = new HashMap<>();

        model.put("issuedDay", String.format("%02d", issuedDate.getDayOfMonth()));
        model.put("issuedMonth", String.format("%02d", issuedDate.getMonthValue()));
        model.put("issuedYear", String.valueOf(issuedDate.getYear()));

        // Người đề nghị = người cấp phiếu công tác (Tổ trưởng/Quản đốc tạo PCT).
        Account createdBy = workOrder.getCreatedBy();
        model.put("requesterDepartment", departmentOf(createdBy));
        model.put("requesterName", nameOf(createdBy));
        model.put("requesterPosition", positionOf(createdBy));

        model.put("recipientDepartment", "Bộ phận kho vật tư");
        model.put("reason", "Cấp phát vật tư, trang thiết bị phục vụ sửa chữa theo phiếu công tác");
        model.put("orderCode", workOrder.getOrderCode());
        model.put("description", workOrder.getRepairDescription() != null
                ? workOrder.getRepairDescription() : DOTS);

        model.put("itemRows", itemRows);
        return model;
    }

    private static String nameOf(Account account) {
        if (account == null) {
            return DOTS;
        }
        return account.getEmployee() != null && account.getEmployee().getFullName() != null
                ? account.getEmployee().getFullName()
                : account.getUsername();
    }

    private static String positionOf(Account account) {
        return account != null && account.getEmployee() != null
                && account.getEmployee().getPosition() != null
                ? account.getEmployee().getPosition().getName()
                : DOTS;
    }

    private static String departmentOf(Account account) {
        return account != null && account.getEmployee() != null
                && account.getEmployee().getDepartment() != null
                ? account.getEmployee().getDepartment().getName()
                : DOTS;
    }
}
