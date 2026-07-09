package com.example.m6_thermal_power_plant_api.service.pdf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Render template pdf/issue.html qua PDFService THẬT nhưng KHÔNG boot Spring
 * context (không cần MySQL) — bắt sớm 2 lỗi hay gặp của template PDF:
 * XHTML không well-formed (OpenHTMLtoPDF ném lỗi parse) và biểu thức
 * Thymeleaf sai tên biến.
 */
class SuppliesIssueTemplateRenderTest {

    private PDFService pdfService;

    @BeforeEach
    void setUp() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);

        pdfService = new PDFService(engine);
        pdfService.loadFonts();
    }

    @Test
    void renderIssueTemplate_producesValidPdfBytes() throws IOException {
        List<Map<String, String>> itemRows = new ArrayList<>();
        itemRows.add(Map.of("name", "Vòng bi SKF 6205", "code", "SP-0001",
                "unit", "Cái", "quantity", "2"));
        itemRows.add(Map.of("name", "Mỡ bôi trơn chịu nhiệt", "code", "CS-0004",
                "unit", "Kg", "quantity", "1.50"));
        // Dòng đệm giữ chỗ như mẫu giấy (service pad tối thiểu 5 dòng) — STT để trống.
        while (itemRows.size() < 5) {
            itemRows.add(Map.of("name", "", "code", "", "unit", "", "quantity", ""));
        }

        Map<String, Object> model = new HashMap<>();
        model.put("issuedDay", "07");
        model.put("issuedMonth", "07");
        model.put("issuedYear", "2026");
        model.put("requesterDepartment", "Phân xưởng Sửa chữa");
        model.put("recipientDepartment", "Bộ phận kho vật tư");
        model.put("requesterName", "Nguyễn Văn Tổ Trưởng");
        model.put("requesterPosition", "Tổ trưởng sửa chữa");
        model.put("reason", "Cấp phát vật tư phục vụ sửa chữa theo phiếu công tác");
        model.put("orderCode", "WO-260707080000-001");
        model.put("description", "Thay vòng bi bơm cấp nước lò hơi — tiếng Việt có dấu: ắ ầ ậ ễ ỗ ợ ữ");
        model.put("itemRows", itemRows);

        byte[] pdf = pdfService.renderPdf("pdf/issue", model);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");

        Path out = Path.of("build/supplies-issue-test.pdf");
        Files.createDirectories(out.getParent());
        Files.write(out, pdf);
        System.out.println("Da ghi PDF phieu cap vat tu mau: " + out.toAbsolutePath());
    }
}
