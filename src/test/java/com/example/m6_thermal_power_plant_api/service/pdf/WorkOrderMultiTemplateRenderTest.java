package com.example.m6_thermal_power_plant_api.service.pdf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Render template pdf/work-order-multi.html qua PDFService THẬT (không boot
 * Spring) — bắt sớm XHTML không well-formed và biểu thức Thymeleaf sai tên.
 */
class WorkOrderMultiTemplateRenderTest {

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
    void renderWorkOrderMultiTemplate_producesValidPdfBytes() throws IOException {
        Map<String, Object> model = new HashMap<>();
        model.put("orderCode", "WO-test-001");
        model.put("issuerDepartment", "Phan xuong sua chua");
        model.put("issuerName", "Nguyen Van An");
        model.put("issuerPosition", "Quan doc");
        model.put("leaderName", "Tran Thi Binh");
        model.put("directSupervisorName", "Le Minh Cuong");
        model.put("safetySupervisorName", "Pham Van Dat");
        model.put("location", "..........");
        model.put("description", "Sua toan bo he thong");
        model.put("plannedStartHour", "08");
        model.put("plannedStartMinute", "00");
        model.put("plannedStartDay", "01");
        model.put("plannedStartMonth", "08");
        model.put("plannedStartYear", "2026");
        model.put("actualEndHour", "......");
        model.put("actualEndMinute", "......");
        model.put("actualEndDay", "......");
        model.put("actualEndMonth", "......");
        model.put("actualEndYear", "......");
        model.put("issuedDay", "01");
        model.put("issuedMonth", "08");
        model.put("issuedYear", "2026");
        model.put("actualStartHour", "......");
        model.put("actualStartMinute", "......");
        model.put("actualStartDay", "......");
        model.put("actualStartMonth", "......");
        model.put("actualStartYear", "......");
        model.put("memberCount", 2L);
        model.put("memberRows", List.of(
                Map.of("name", "Hoang Quoc Dat", "joinedAt", "08:00 01/08/2026", "leftAt", ""),
                Map.of("name", "", "joinedAt", "", "leftAt", ""),
                Map.of("name", "", "joinedAt", "", "leftAt", ""),
                Map.of("name", "", "joinedAt", "", "leftAt", ""),
                Map.of("name", "", "joinedAt", "", "leftAt", "")));
        model.put("extensionRows", List.of(
                Map.of("stoppedAt", "", "reason", "", "allowedDate", ""),
                Map.of("stoppedAt", "", "reason", "", "allowedDate", ""),
                Map.of("stoppedAt", "", "reason", "", "allowedDate", ""),
                Map.of("stoppedAt", "", "reason", "", "allowedDate", ""),
                Map.of("stoppedAt", "", "reason", "", "allowedDate", "")));
        model.put("equipmentRows", List.of(
                Map.of("kksCode", "KKS-1", "name", "Quat gio A", "systemName", "He thong nhien lieu"),
                Map.of("kksCode", "KKS-2", "name", "Quat gio B", "systemName", "")));

        byte[] pdf = pdfService.renderPdf("pdf/work-order-multi", model);

        assertThat(pdf).isNotEmpty();
        assertThat(pdf).startsWith(new byte[]{(byte) 0x25, (byte) 0x50, (byte) 0x44, (byte) 0x46});
    }
}