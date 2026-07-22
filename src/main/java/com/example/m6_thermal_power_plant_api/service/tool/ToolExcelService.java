package com.example.m6_thermal_power_plant_api.service.tool;

import com.example.m6_thermal_power_plant_api.dto.tool.ToolImportResult;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolImportRowResult;
import com.example.m6_thermal_power_plant_api.entity.tool.Tool;
import com.example.m6_thermal_power_plant_api.entity.tool.ToolCategory;
import com.example.m6_thermal_power_plant_api.exception.BadRequestException;
import com.example.m6_thermal_power_plant_api.repository.IToolCategoryRepository;
import com.example.m6_thermal_power_plant_api.repository.IToolRepository;
import com.example.m6_thermal_power_plant_api.util.TimeStampCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Xử lý import/export CCDC bằng file Excel (.xlsx).
 * Quy tắc nghiệp vụ:
 *  - Cột: Tên CCDC | Chủng loại | Đơn vị tính | Số lượng | Ghi chú
 *  - Mã CCDC do hệ thống tự sinh (không nhập trong Excel)
 *  - All-or-nothing: chỉ import khi TẤT CẢ dòng hợp lệ
 *  - Chủng loại phải tồn tại sẵn (không tự tạo mới)
 */
@Service
@RequiredArgsConstructor
public class ToolExcelService {

    private static final String[] HEADERS = {"Tên CCDC", "Chủng loại", "Đơn vị tính", "Số lượng", "Ghi chú"};

    private final IToolRepository toolRepository;
    private final IToolCategoryRepository toolCategoryRepository;

    /** Sinh file Excel mẫu: 1 sheet dữ liệu (có tiêu đề + 1 dòng ví dụ) + 1 sheet hướng dẫn liệt kê chủng loại. */
    public byte[] buildTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Sheet dữ liệu
            Sheet sheet = workbook.createSheet("CCDC");
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 6000);
            }

            // Dòng ví dụ để người dùng biết cách điền
            Row example = sheet.createRow(1);
            example.createCell(0).setCellValue("Bộ cờ lê 12 món");
            example.createCell(1).setCellValue("(điền đúng tên chủng loại ở sheet Hướng dẫn)");
            example.createCell(2).setCellValue("Bộ");
            example.createCell(3).setCellValue(5);
            example.createCell(4).setCellValue("Ghi chú nếu có");

            // Sheet hướng dẫn — liệt kê các chủng loại đang có
            Sheet guide = workbook.createSheet("Hướng dẫn");
            Row g0 = guide.createRow(0);
            Cell gc0 = g0.createCell(0);
            gc0.setCellValue("Các chủng loại hợp lệ (điền đúng tên vào cột 'Chủng loại'):");
            gc0.setCellStyle(headerStyle);
            guide.setColumnWidth(0, 10000);

            List<ToolCategory> categories = toolCategoryRepository.findAll();
            int r = 1;
            for (ToolCategory c : categories) {
                guide.createRow(r++).createCell(0).setCellValue(c.getCategoryName());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi tạo file Excel mẫu", e);
        }
    }

    /** Đọc + kiểm tra file, KHÔNG lưu. Trả về kết quả preview để FE hiển thị. */
    public ToolImportResult preview(MultipartFile file) {
        List<ToolImportRowResult> rows = parseAndValidate(file);
        return summarize(rows);
    }

    /** Import thực sự — all-or-nothing: nếu còn dòng lỗi thì không lưu gì cả. */
    @Transactional
    public ToolImportResult importTools(MultipartFile file) {
        List<ToolImportRowResult> rows = parseAndValidate(file);
        ToolImportResult result = summarize(rows);

        if (!result.isAllValid()) {
            throw new BadRequestException("File còn " + result.getErrorCount()
                    + " dòng lỗi. Vui lòng sửa hết rồi import lại (không dòng nào được nhập).");
        }

        // Sinh mã theo timestamp + hậu tố tự tăng (TimeStampCodeGenerator) —
        // luôn duy nhất, tránh trùng như cách cộng dồn MAX(tool_code) cũ.
        for (ToolImportRowResult row : rows) {
            ToolCategory category = toolCategoryRepository
                    .findFirstByCategoryNameIgnoreCase(row.getCategoryName().trim())
                    .orElseThrow(() -> new BadRequestException("Chủng loại không tồn tại: " + row.getCategoryName()));

            Tool tool = Tool.builder()
                    .toolCode(TimeStampCodeGenerator.generate(Tool.class))
                    .name(row.getName().trim())
                    .toolCategory(category)
                    .unit(row.getUnit().trim())
                    .quantity(row.getQuantity())
                    .note(row.getNote())
                    .build();
            toolRepository.save(tool);
        }
        return result;
    }

    // ----------------------------------------------------------------------

    private List<ToolImportRowResult> parseAndValidate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Chưa chọn file Excel");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            throw new BadRequestException("Chỉ chấp nhận file Excel định dạng .xlsx");
        }

        List<ToolImportRowResult> rows = new ArrayList<>();
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            // Bỏ dòng 0 (tiêu đề), đọc từ dòng 1
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row, formatter)) continue;

                String name = formatter.formatCellValue(row.getCell(0)).trim();
                String categoryName = formatter.formatCellValue(row.getCell(1)).trim();
                String unit = formatter.formatCellValue(row.getCell(2)).trim();
                String quantityStr = formatter.formatCellValue(row.getCell(3)).trim();
                String note = formatter.formatCellValue(row.getCell(4)).trim();

                ToolImportRowResult.ToolImportRowResultBuilder b = ToolImportRowResult.builder()
                        .rowNumber(i + 1)   // hiển thị số dòng như trong Excel (1-based)
                        .name(name)
                        .categoryName(categoryName)
                        .unit(unit)
                        .note(note);

                String error = validateRow(name, categoryName, unit, quantityStr, b);
                if (error == null) {
                    b.valid(true);
                } else {
                    b.valid(false).error(error);
                }
                rows.add(b.build());
            }
        } catch (IOException e) {
            throw new BadRequestException("Không đọc được file Excel: " + e.getMessage());
        }

        if (rows.isEmpty()) {
            throw new BadRequestException("File không có dòng dữ liệu nào");
        }
        return rows;
    }

    /** Trả về null nếu hợp lệ, ngược lại trả về mô tả lỗi. Set luôn quantity vào builder khi parse được. */
    private String validateRow(String name, String categoryName, String unit,
                               String quantityStr, ToolImportRowResult.ToolImportRowResultBuilder b) {
        if (name.isBlank()) return "Tên CCDC trống";
        if (categoryName.isBlank()) return "Chủng loại trống";
        if (toolCategoryRepository.findFirstByCategoryNameIgnoreCase(categoryName).isEmpty()) {
            return "Chủng loại không tồn tại: " + categoryName;
        }
        if (unit.isBlank()) return "Đơn vị tính trống";

        if (quantityStr.isBlank()) return "Số lượng trống";
        int quantity;
        try {
            // DataFormatter có thể trả "5.0" cho ô số → parse qua double rồi ép int
            quantity = (int) Double.parseDouble(quantityStr);
        } catch (NumberFormatException e) {
            return "Số lượng không phải là số: " + quantityStr;
        }
        if (quantity < 0) return "Số lượng không được âm";

        b.quantity(quantity);
        return null;
    }

    private ToolImportResult summarize(List<ToolImportRowResult> rows) {
        int validCount = (int) rows.stream().filter(ToolImportRowResult::isValid).count();
        int errorCount = rows.size() - validCount;
        return ToolImportResult.builder()
                .totalRows(rows.size())
                .validCount(validCount)
                .errorCount(errorCount)
                .allValid(errorCount == 0)
                .rows(rows)
                .build();
    }

    private boolean isRowEmpty(Row row, DataFormatter formatter) {
        for (int c = 0; c < HEADERS.length; c++) {
            if (!formatter.formatCellValue(row.getCell(c)).trim().isEmpty()) return false;
        }
        return true;
    }
}
