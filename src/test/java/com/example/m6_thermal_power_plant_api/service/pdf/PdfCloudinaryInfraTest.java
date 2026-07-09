package com.example.m6_thermal_power_plant_api.service.pdf;

import com.example.m6_thermal_power_plant_api.dto.file.FileUploadResult;
import com.example.m6_thermal_power_plant_api.service.util.FileUploadService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Kiểm tra thủ công hạ tầng PDF + Cloudinary (không gắn nghiệp vụ nào).
 *
 * YÊU CẦU: MySQL đang chạy (context boot cần validate schema). Test (2) gọi mạng
 * thật lên Cloudinary — vì vậy cả class tag "manual", không chạy trong CI/CD.
 *
 * CÁCH CHẠY ĐỂ QUAN SÁT:
 *  1. {@link #renderSamplePdf_producesValidPdfBytes()} → sinh build/sample-a4-test.pdf,
 *     mở file kiểm tra tiếng Việt có dấu hiển thị đúng (font đã nhúng).
 *  2. {@link #uploadAndDeletePdf_onCloudinary_roundTrips()} → in secureUrl ra console,
 *     mở URL trong browser xác nhận tải được PDF (nhớ bật Settings -> Security ->
 *     "Allow delivery of PDF and ZIP files" trên Cloudinary dashboard), sau đó
 *     test tự xoá file bằng đúng publicId/resourceType đã lưu.
 */
@SpringBootTest
@Tag("manual")
class PdfCloudinaryInfraTest {

    @Autowired
    private PDFService pdfService;

    @Autowired
    private FileUploadService fileUploadService;

    @Test
    void renderSamplePdf_producesValidPdfBytes() throws IOException {
        Map<String, Object> rows = new LinkedHashMap<>();
        rows.put("Mã phiếu", "TEST-260101-001");
        rows.put("Thiết bị", "Bơm cấp nước lò hơi A (10LAC10AP001)");
        rows.put("Nội dung", "Kiểm tra hạ tầng sinh PDF — tiếng Việt có dấu: ắ ầ ậ ễ ỗ ợ ữ");

        byte[] pdf = pdfService.renderPdf("pdf/sample-a4", Map.of(
                "title", "Phiếu kiểm tra hạ tầng PDF",
                "createdAt", LocalDateTime.now(),
                "rows", rows
        ));

        assertThat(pdf).isNotEmpty();
        // Magic bytes của mọi file PDF hợp lệ
        assertThat(new String(pdf, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");

        Path out = Path.of("build/sample-a4-test.pdf");
        Files.createDirectories(out.getParent());
        Files.write(out, pdf);
        System.out.println("Da ghi PDF mau: " + out.toAbsolutePath() + " — mo file kiem tra tieng Viet.");
    }

    @Test
    void uploadAndDeletePdf_onCloudinary_roundTrips() throws IOException {
        byte[] pdf = pdfService.renderPdf("pdf/sample-a4", Map.of(
                "title", "Upload round-trip test",
                "createdAt", LocalDateTime.now()
        ));

        FileUploadResult result = fileUploadService.uploadPdf(
                pdf, "infra-test", "upload-test-" + System.currentTimeMillis());

        System.out.println("publicId    : " + result.publicId());
        System.out.println("resourceType: " + result.resourceType());
        System.out.println("secureUrl   : " + result.secureUrl());

        assertThat(result.publicId()).startsWith("infra-test/");
        assertThat(result.secureUrl()).startsWith("https://");

        // Xoá bằng đúng cặp (publicId, resourceType) đã lưu — deleteFile ném
        // IllegalStateException nếu Cloudinary trả về khác "ok" (fail to, không im lặng).
        fileUploadService.deleteFile(result.publicId(), result.resourceType());
    }
}
