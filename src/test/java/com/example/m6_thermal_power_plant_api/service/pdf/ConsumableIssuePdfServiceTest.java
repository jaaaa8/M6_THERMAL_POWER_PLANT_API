package com.example.m6_thermal_power_plant_api.service.pdf;

import com.example.m6_thermal_power_plant_api.dto.file.FileUploadResult;
import com.example.m6_thermal_power_plant_api.entity.Consumable;
import com.example.m6_thermal_power_plant_api.entity.ConsumableIssue;
import com.example.m6_thermal_power_plant_api.entity.ConsumableIssueDetail;
import com.example.m6_thermal_power_plant_api.entity.Department;
import com.example.m6_thermal_power_plant_api.entity.Employee;
import com.example.m6_thermal_power_plant_api.entity.Position;
import com.example.m6_thermal_power_plant_api.entity.Unit;
import com.example.m6_thermal_power_plant_api.entity.WorkOrder;
import com.example.m6_thermal_power_plant_api.exception.ObjectNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.IConsumableIssueDetailRepository;
import com.example.m6_thermal_power_plant_api.repository.IConsumableIssueRepository;
import com.example.m6_thermal_power_plant_api.service.util.FileUploadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Khoá BỘ KEY mà buildModel đưa sang template pdf/issue.html — lệch tên biến là
 * lỗi âm thầm (Thymeleaf in ra rỗng chứ không ném), nên phải chốt bằng test.
 */
@ExtendWith(MockitoExtension.class)
class ConsumableIssuePdfServiceTest {

    @Mock
    private IConsumableIssueRepository issueRepository;
    @Mock
    private IConsumableIssueDetailRepository detailRepository;
    @Mock
    private PDFService pdfService;
    @Mock
    private FileUploadService fileUploadService;
    @InjectMocks
    private ConsumableIssuePdfService service;

    @Test
    @SuppressWarnings("unchecked")
    void render_buildsModelMatchingIssueTemplate() throws java.io.IOException {
        when(issueRepository.findById(5)).thenReturn(Optional.of(issue()));
        when(detailRepository.findByIssue_Id(5)).thenReturn(List.of(
                detail("Vòng bi SKF 6205", "CS-0001", "Cái", new BigDecimal("2.00")),
                detail("Mỡ bôi trơn", "CS-0004", "Kg", new BigDecimal("1.50"))));
        when(pdfService.renderPdf(eq("pdf/issue"), any())).thenReturn("%PDF".getBytes());
        when(fileUploadService.uploadPdf(any(), eq(ConsumableIssuePdfService.CLOUDINARY_FOLDER),
                eq("CI-20260720-001")))
                .thenReturn(new FileUploadResult("consumable-issues/CI-20260720-001",
                        "https://res.cloudinary.com/demo/CI-20260720-001.pdf", "image", "pdf", 1024));

        ConsumableIssuePdfService.ConsumableIssuePdf result = service.render(10, 5);

        assertThat(result.url()).isEqualTo("https://res.cloudinary.com/demo/CI-20260720-001.pdf");

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(pdfService).renderPdf(eq("pdf/issue"), captor.capture());
        Map<String, Object> model = captor.getValue();

        assertThat(model).containsEntry("issuedDay", "20")
                .containsEntry("issuedMonth", "07")
                .containsEntry("issuedYear", "2026")
                // Người đề nghị = NGƯỜI LÃNH ĐẠO của PCT, không phải người cấp phát.
                .containsEntry("requesterName", "Nguyễn Văn Lãnh")
                .containsEntry("requesterPosition", "Tổ trưởng sửa chữa")
                .containsEntry("requesterDepartment", "Phân xưởng Sửa chữa")
                .containsEntry("orderCode", "WO-20260720-001")
                .containsEntry("description", "Thay vòng bi bơm cấp nước")
                // Không có trong hệ thống -> in dòng chấm để điền tay.
                .containsEntry("recipientDepartment", "..........")
                .containsEntry("reason", "..........");

        List<Map<String, String>> rows = (List<Map<String, String>>) model.get("itemRows");
        assertThat(rows).hasSize(ConsumableIssuePdfService.MIN_ITEM_ROWS);
        assertThat(rows.get(0)).containsEntry("name", "Vòng bi SKF 6205")
                .containsEntry("code", "CS-0001").containsEntry("unit", "Cái")
                .containsEntry("quantity", "2");        // 2.00 -> "2"
        assertThat(rows.get(1)).containsEntry("quantity", "1.5");   // 1.50 -> "1.5"
        // Dòng đệm giữ chỗ: name rỗng nên template bỏ trống cả cột STT.
        assertThat(rows.get(4)).containsEntry("name", "").containsEntry("quantity", "");
    }

    @Test
    void render_whenIssueBelongsToAnotherWorkOrder_throwsNotFound() {
        when(issueRepository.findById(5)).thenReturn(Optional.of(issue()));

        assertThatThrownBy(() -> service.render(999, 5))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    private static ConsumableIssue issue() {
        Department department = new Department();
        department.setName("Phân xưởng Sửa chữa");
        Position position = new Position();
        position.setName("Tổ trưởng sửa chữa");
        Employee leader = new Employee();
        leader.setFullName("Nguyễn Văn Lãnh");
        leader.setPosition(position);
        leader.setDepartment(department);

        WorkOrder workOrder = WorkOrder.builder()
                .id(10).orderCode("WO-20260720-001")
                .repairDescription("Thay vòng bi bơm cấp nước")
                .leader(leader)
                .build();

        return ConsumableIssue.builder()
                .id(5).consumableCode("CI-20260720-001")
                .workOrder(workOrder)
                .issuedAt(LocalDateTime.of(2026, 7, 20, 9, 30))
                .build();
    }

    private static ConsumableIssueDetail detail(String name, String code, String unitName,
                                                BigDecimal quantity) {
        Unit unit = new Unit();
        unit.setName(unitName);
        Consumable consumable = new Consumable();
        consumable.setName(name);
        consumable.setConsumableCode(code);
        consumable.setUnit(unit);
        return ConsumableIssueDetail.builder().consumable(consumable).quantity(quantity).build();
    }
}
