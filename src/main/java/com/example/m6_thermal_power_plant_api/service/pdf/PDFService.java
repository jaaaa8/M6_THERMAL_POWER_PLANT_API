package com.example.m6_thermal_power_plant_api.service.pdf;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

/**
 * Sinh PDF từ Thymeleaf template — hạ tầng DÙNG CHUNG, không gắn nghiệp vụ nào.
 * Mỗi nghiệp vụ (phiếu công tác, phiếu đánh giá...) tự tạo template HTML trong
 * src/main/resources/templates/pdf/ rồi gọi {@link #renderPdf}.
 *
 * Stack: Thymeleaf render HTML -> OpenHTMLtoPDF (io.github.openhtmltopdf, fork
 * đang được maintain của Flying Saucer) convert sang PDF. Khác bản cũ ở
 * office_rental_md5: font chỉ nạp MỘT lần lúc khởi động (bản cũ đọc lại
 * times.ttf mỗi lần gọi), và lib mới hỗ trợ CSS tốt hơn.
 *
 * FONT: nhúng ĐỦ 4 biến thể (regular/bold/italic/bold-italic) — đây là fix một
 * lỗi phát hiện lúc verify: nếu chỉ nạp mỗi bản Regular, chữ {@code font-weight:
 * bold} trong template bị OpenHTMLtoPDF "giả lập đậm" (vẽ đè outline lệch), làm
 * dấu tiếng Việt (đặc biệt dấu đôi như ắ ầ ậ) hiển thị lem/nhoè. Nhúng đúng file
 * Bold thật thì chữ đậm dùng glyph thật, không còn hiện tượng này.
 *
 * QUY TẮC VIẾT TEMPLATE (xem mẫu templates/pdf/sample-a4.html):
 *  - Phải là XHTML well-formed: mọi tag tự đóng (&lt;br/&gt;, &lt;img .../&gt;),
 *    attribute có nháy — HTML lỏng lẻo (thiếu đóng tag) sẽ ném lỗi parse.
 *  - CSS phải khai {@code font-family: 'Times New Roman'} (đúng tên đăng ký ở
 *    đây) thì chữ mới dùng đúng font đã nhúng — font hệ thống không có sẵn trên
 *    server thì trình render dùng font thay thế, tiếng Việt vỡ dấu.
 *  - font-weight: bold / font-style: italic dùng bình thường — đã có glyph thật
 *    cho cả 4 tổ hợp weight/style.
 *  - Khổ giấy khai bằng CSS: {@code @page { size: A4; margin: 2cm; }}.
 */
@Service
@RequiredArgsConstructor
public class PDFService {

    /** Tên font-family template CSS phải dùng để chữ tiếng Việt hiển thị đúng. */
    public static final String FONT_FAMILY = "Times New Roman";

    private static final String FONT_DIR = "fonts/";
    private static final String REGULAR = "times.ttf";
    private static final String BOLD = "timesbd.ttf";
    private static final String ITALIC = "timesi.ttf";
    private static final String BOLD_ITALIC = "timesbi.ttf";

    private final SpringTemplateEngine templateEngine;

    /** Nạp 1 lần lúc khởi động — tránh đọc lại file font mỗi lần render. */
    private byte[] regularBytes;
    private byte[] boldBytes;
    private byte[] italicBytes;
    private byte[] boldItalicBytes;

    @PostConstruct
    void loadFonts() {
        regularBytes = readFont(REGULAR);
        boldBytes = readFont(BOLD);
        italicBytes = readFont(ITALIC);
        boldItalicBytes = readFont(BOLD_ITALIC);
    }

    private static byte[] readFont(String fileName) {
        try {
            return new ClassPathResource(FONT_DIR + fileName).getInputStream().readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Khong doc duoc font " + FONT_DIR + fileName + " — PDF tieng Viet se vo dau.", e);
        }
    }

    /**
     * Render một Thymeleaf template thành PDF.
     *
     * @param templateName đường dẫn template tính từ templates/, KHÔNG đuôi .html
     *                     — VD {@code "pdf/sample-a4"}
     * @param model        biến truyền vào template (truy cập bằng ${...})
     * @return nội dung file PDF
     */
    public byte[] renderPdf(String templateName, Map<String, Object> model) {
        Context context = new Context();
        if (model != null) {
            context.setVariables(model);
        }
        String html = templateEngine.process(templateName, context);
        return htmlToPdf(html);
    }

    /**
     * Convert chuỗi XHTML (đã render sẵn) thành PDF. Public để nghiệp vụ nào tự
     * ghép HTML ngoài Thymeleaf vẫn dùng được phần convert + font.
     */
    public byte[] htmlToPdf(String xhtml) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            // Nhúng đủ 4 biến thể (subset=true) — máy mở file không cần cài font,
            // và chữ đậm/nghiêng dùng glyph thật thay vì giả lập.
            builder.useFont(() -> new ByteArrayInputStream(regularBytes),
                    FONT_FAMILY, 400, FontStyle.NORMAL, true);
            builder.useFont(() -> new ByteArrayInputStream(boldBytes),
                    FONT_FAMILY, 700, FontStyle.NORMAL, true);
            builder.useFont(() -> new ByteArrayInputStream(italicBytes),
                    FONT_FAMILY, 400, FontStyle.ITALIC, true);
            builder.useFont(() -> new ByteArrayInputStream(boldItalicBytes),
                    FONT_FAMILY, 700, FontStyle.ITALIC, true);
            builder.withHtmlContent(xhtml, null);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Sinh PDF that bai.", e);
        }
    }
}
