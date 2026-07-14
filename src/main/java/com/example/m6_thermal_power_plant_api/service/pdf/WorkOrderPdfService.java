package com.example.m6_thermal_power_plant_api.service.pdf;

import com.example.m6_thermal_power_plant_api.dto.file.FileUploadResult;
import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.Employee;
import com.example.m6_thermal_power_plant_api.entity.WorkOrder;
import com.example.m6_thermal_power_plant_api.entity.WorkOrderExtension;
import com.example.m6_thermal_power_plant_api.entity.WorkOrderMember;
import com.example.m6_thermal_power_plant_api.entity.enums.WorkOrderStatus;
import com.example.m6_thermal_power_plant_api.exception.ObjectNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.WorkOrderExtensionRepository;
import com.example.m6_thermal_power_plant_api.repository.WorkOrderMemberRepository;
import com.example.m6_thermal_power_plant_api.repository.WorkOrderRepository;
import com.example.m6_thermal_power_plant_api.service.util.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sinh bản in PDF của Phiếu Công Tác (PCT) theo mẫu giấy nhà máy.
 *
 * Mỗi lần gọi render một SNAPSHOT mới từ dữ liệu hiện tại (member vào/ra, gia hạn
 * thay đổi liên tục trong đời phiếu), upload lên Cloudinary folder "work-orders"
 * với public_id = orderCode (overwrite — bản mới đè bản cũ, pdf_path không đổi
 * giữa các lần render) rồi lưu URL vào work_orders.pdf_path.
 *
 * Các mục trên phiếu KHÔNG có trong hệ thống (Người cho phép, giờ cho phép bắt
 * đầu của Trưởng ca, cột giờ bắt đầu/kết thúc của bảng gia hạn hàng ngày) được
 * in dòng chấm (…) để Trưởng ca điền TAY — đúng quy trình vận hành theo ca.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkOrderPdfService {

    /** Thư mục Cloudinary chứa PDF phiếu công tác. */
    static final String CLOUDINARY_FOLDER = "work-orders";
    /** Số dòng tối thiểu của bảng "cho phép làm việc hàng ngày" (kể cả dòng trống điền tay). */
    static final int MIN_EXTENSION_ROWS = 5;
    /** Số dòng tối thiểu của bảng vào/ra vị trí làm việc. */
    static final int MIN_MEMBER_ROWS = 5;

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_DATE = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
    private static final String DOTS = "..........";

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderMemberRepository workOrderMemberRepository;
    private final WorkOrderExtensionRepository workOrderExtensionRepository;
    private final PDFService pdfService;
    private final FileUploadService fileUploadService;

    /** Kết quả render: nội dung PDF + URL Cloudinary (null nếu upload lỗi). */
    public record WorkOrderPdf(String orderCode, byte[] content, String url) {
    }

    /**
     * Render PDF phiếu công tác cho việc XUẤT/IN.
     *
     * Phiếu còn sống: mỗi lần render một snapshot mới + upload đè bản Cloudinary
     * cũ + lưu pdf_path (upload lỗi KHÔNG làm hỏng việc tải phiếu — vẫn trả về
     * nội dung PDF, chỉ log cảnh báo và giữ pdf_path cũ).
     *
     * Phiếu ĐÃ KẾT THÚC (COMPLETED/CANCELLED) và đã có bản lưu: vẫn trả về bytes
     * render mới (dữ liệu sau kết thúc bất biến — mọi thao tác ghi đều bị chặn
     * theo status) nhưng KHÔNG upload đè — pdf_path là bản lưu ĐÓNG BĂNG do
     * {@link #archive} ghi lúc đóng phiếu, không được trôi khỏi bản giấy đã ký.
     */
    @Transactional
    public WorkOrderPdf render(Integer workOrderId) {
        WorkOrder workOrder = loadWorkOrder(workOrderId);
        byte[] pdf = renderBytes(workOrder);

        if (isTerminal(workOrder) && workOrder.getPdfPath() != null) {
            return new WorkOrderPdf(workOrder.getOrderCode(), pdf, workOrder.getPdfPath());
        }
        String url = uploadAndSave(workOrder, pdf);
        return new WorkOrderPdf(workOrder.getOrderCode(), pdf, url);
    }

    /**
     * ĐÓNG BĂNG bản lưu cuối cùng khi phiếu về trạng thái kết thúc: render
     * snapshot chốt sổ + upload đè + lưu pdf_path (bỏ qua guard chống đè của
     * {@link #render}). Chỉ gọi từ luồng complete/cancel — sau thời điểm này
     * {@link #render} không đụng tới Cloudinary nữa.
     */
    @Transactional
    public void archive(Integer workOrderId) {
        WorkOrder workOrder = loadWorkOrder(workOrderId);
        uploadAndSave(workOrder, renderBytes(workOrder));
    }

    private WorkOrder loadWorkOrder(Integer workOrderId) {
        return workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Khong tim thay phieu cong tac voi id: " + workOrderId));
    }

    private byte[] renderBytes(WorkOrder workOrder) {
        List<WorkOrderMember> members = workOrderMemberRepository.findByWorkOrder_Id(workOrder.getId());
        List<WorkOrderExtension> extensions =
                workOrderExtensionRepository.findByWorkOrder_IdOrderByExtendedUntilAsc(workOrder.getId());
        return pdfService.renderPdf("pdf/work-order", buildModel(workOrder, members, extensions));
    }

    /** Upload đè bản cũ cùng orderCode + lưu pdf_path. Trả về URL, hoặc null nếu upload lỗi. */
    private String uploadAndSave(WorkOrder workOrder, byte[] pdf) {
        try {
            FileUploadResult uploaded =
                    fileUploadService.uploadPdf(pdf, CLOUDINARY_FOLDER, workOrder.getOrderCode());
            workOrder.setPdfPath(uploaded.secureUrl());
            workOrderRepository.save(workOrder);
            return uploaded.secureUrl();
        } catch (IOException e) {
            log.warn("Upload PDF phieu cong tac {} len Cloudinary that bai — van tra ve noi dung PDF.",
                    workOrder.getOrderCode(), e);
            return null;
        }
    }

    private static boolean isTerminal(WorkOrder workOrder) {
        return workOrder.getStatus() == WorkOrderStatus.COMPLETED
                || workOrder.getStatus() == WorkOrderStatus.CANCELLED;
    }

    private Map<String, Object> buildModel(WorkOrder workOrder, List<WorkOrderMember> members,
                                           List<WorkOrderExtension> extensions) {
        Map<String, Object> model = new HashMap<>();

        model.put("orderCode", workOrder.getOrderCode());
        model.put("issuerDepartment", departmentOf(workOrder.getCreatedBy()));
        model.put("issuerName", nameOf(workOrder.getCreatedBy()));
        model.put("issuerPosition", positionOf(workOrder.getCreatedBy()));

        model.put("leaderName", nameOf(workOrder.getLeader()));
        model.put("directSupervisorName", nameOf(workOrder.getDirectSupervisor()));
        model.put("safetySupervisorName", nameOf(workOrder.getSafetySupervisor()));

        // Địa điểm công tác: hệ thống thiết bị (kèm tên thiết bị) từ yêu cầu sửa chữa.
        String location = DOTS;
        if (workOrder.getRepairRequest() != null && workOrder.getRepairRequest().getEquipment() != null) {
            var equipment = workOrder.getRepairRequest().getEquipment();
            location = equipment.getSystem() != null ? equipment.getSystem().getName() : DOTS;
            if (equipment.getName() != null) {
                location += " — " + equipment.getName();
            }
        }
        model.put("location", location);

        model.put("description", workOrder.getRepairDescription() != null
                ? workOrder.getRepairDescription() : DOTS);
        model.put("plannedFrom", format(workOrder.getStartTime(), TIME_DATE));
        model.put("plannedTo", format(workOrder.getExpectedEndTime(), TIME_DATE));

        LocalDateTime issued = workOrder.getCreatedAt();
        model.put("issuedDay", issued != null ? String.format("%02d", issued.getDayOfMonth()) : "......");
        model.put("issuedMonth", issued != null ? String.format("%02d", issued.getMonthValue()) : "......");
        model.put("issuedYear", issued != null ? String.valueOf(issued.getYear()) : "......");

        // Bắt đầu tiến hành công việc = người ĐẦU TIÊN vào vị trí làm việc.
        LocalDateTime actualStart = members.stream()
                .map(WorkOrderMember::getJoinedAt)
                .filter(java.util.Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
        model.put("actualStartHour", actualStart != null ? String.format("%02d", actualStart.getHour()) : "......");
        model.put("actualStartMinute", actualStart != null ? String.format("%02d", actualStart.getMinute()) : "......");
        model.put("actualStartDay", actualStart != null ? String.format("%02d", actualStart.getDayOfMonth()) : "......");
        model.put("actualStartMonth", actualStart != null ? String.format("%02d", actualStart.getMonthValue()) : "......");
        model.put("actualStartYear", actualStart != null ? String.valueOf(actualStart.getYear()) : "......");

        // Số NHÂN VIÊN (distinct) — 1 người rời rồi vào lại tạo 2 dòng member nhưng vẫn là 1 người.
        long memberCount = members.stream()
                .map(m -> m.getEmployees() != null ? m.getEmployees().getId() : null)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .count();
        model.put("memberCount", memberCount);

        // Bảng vào/ra vị trí: mỗi dòng member một cặp JOINED/LEFT, theo giờ vào tăng dần.
        List<Map<String, String>> memberRows = new ArrayList<>();
        members.stream()
                .sorted(Comparator.comparing(WorkOrderMember::getJoinedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .forEach(m -> {
                    Map<String, String> row = new HashMap<>();
                    row.put("name", m.getEmployees() != null ? m.getEmployees().getFullName() : DOTS);
                    row.put("role", m.getRoleInTask() != null ? m.getRoleInTask() : "");
                    row.put("joinedAt", format(m.getJoinedAt(), TIME_DATE));
                    row.put("leftAt", format(m.getLeftAt(), TIME_DATE));
                    memberRows.add(row);
                });
        while (memberRows.size() < MIN_MEMBER_ROWS) {
            memberRows.add(Map.of("name", "", "role", "", "joinedAt", "", "leftAt", ""));
        }
        model.put("memberRows", memberRows);

        // Bảng cho phép làm việc hàng ngày: in các gia hạn đã có (giờ bắt đầu/kết
        // thúc từng ngày Trưởng ca điền TAY), bù thêm dòng trống cho những ngày sau.
        List<Map<String, String>> extensionRows = new ArrayList<>();
        for (WorkOrderExtension extension : extensions) {
            Map<String, String> row = new HashMap<>();
            row.put("allowedUntil", format(extension.getExtendedUntil(), DATE));
            row.put("reason", extension.getReason() != null ? extension.getReason() : "");
            row.put("approvedBy", nameOf(extension.getApprovedBy()));
            extensionRows.add(row);
        }
        while (extensionRows.size() < MIN_EXTENSION_ROWS) {
            extensionRows.add(Map.of("allowedUntil", "", "reason", "", "approvedBy", ""));
        }
        model.put("extensionRows", extensionRows);

        return model;
    }

    private static String format(LocalDateTime value, DateTimeFormatter formatter) {
        return value != null ? value.format(formatter) : "";
    }

    private static String nameOf(Employee employee) {
        return employee != null && employee.getFullName() != null ? employee.getFullName() : DOTS;
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
