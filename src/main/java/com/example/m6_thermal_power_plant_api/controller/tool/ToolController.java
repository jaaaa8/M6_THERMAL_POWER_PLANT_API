package com.example.m6_thermal_power_plant_api.controller.tool;

import com.example.m6_thermal_power_plant_api.dto.tool.ApiResponse;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolDamageRequest;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolImportResult;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolQuantityUpdateRequest;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolRequest;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolResponse;
import com.example.m6_thermal_power_plant_api.dto.tool.ToolTransactionLogResponse;
import com.example.m6_thermal_power_plant_api.service.tool.ToolExcelService;
import com.example.m6_thermal_power_plant_api.service.impl.IToolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tools")
@RequiredArgsConstructor
public class ToolController {

    private final IToolService toolService;
    private final ToolExcelService toolExcelService;

    @PreAuthorize("hasAnyRole('TOOLS_STOREKEEPER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ToolResponse> create(@Valid @RequestBody ToolRequest request) {
        return ApiResponse.success("Tạo CCDC thành công", toolService.create(request));
    }

    @PreAuthorize("hasAnyRole('TOOLS_STOREKEEPER')")
    @PutMapping("/{id}")
    public ApiResponse<ToolResponse> update(@PathVariable Integer id, @Valid @RequestBody ToolRequest request) {
        return ApiResponse.success("Cập nhật CCDC thành công", toolService.update(id, request));
    }

    @PreAuthorize("hasAnyRole('TOOLS_STOREKEEPER')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        toolService.delete(id);
        return ApiResponse.success("Xoá CCDC thành công", null);
    }

    @GetMapping("/{id}")
    public ApiResponse<ToolResponse> getById(@PathVariable Integer id) {
        return ApiResponse.success(toolService.getById(id));
    }

    @GetMapping
    public ApiResponse<Page<ToolResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            Pageable pageable) {
        return ApiResponse.success(toolService.search(keyword, categoryId, pageable));
    }

    @PreAuthorize("hasAnyRole('TOOLS_STOREKEEPER')")
    @PatchMapping("/{id}/quantity")
    public ApiResponse<ToolResponse> addQuantity(@PathVariable Integer id,
                                                  @Valid @RequestBody ToolQuantityUpdateRequest request) {
        return ApiResponse.success("Nhập kho thành công", toolService.addQuantity(id, request));
    }

    @PreAuthorize("hasAnyRole('TOOLS_STOREKEEPER')")
    @PatchMapping("/{id}/damage")
    public ApiResponse<ToolResponse> markDamaged(@PathVariable Integer id,
                                                  @Valid @RequestBody ToolDamageRequest request) {
        return ApiResponse.success("Đã huỷ CCDC hư hỏng", toolService.markDamaged(id, request));
    }

    @GetMapping("/{id}/logs")
    public ApiResponse<List<ToolTransactionLogResponse>> getTransactionLogs(@PathVariable Integer id) {
        return ApiResponse.success(toolService.getTransactionLogs(id));
    }

    @GetMapping("/next-code")
    public ApiResponse<String> getNextCode() {
        return ApiResponse.success(toolService.generateNextCode());
    }

    /** Tải file Excel mẫu để nhập CCDC hàng loạt */
    @GetMapping("/import/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] file = toolExcelService.buildTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"mau-nhap-ccdc.xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    /** Xem trước dữ liệu import (chưa lưu) — trả về kết quả kiểm tra từng dòng */
    @PreAuthorize("hasAnyRole('TOOLS_STOREKEEPER')")
    @PostMapping(value = "/import/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ToolImportResult> previewImport(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(toolExcelService.preview(file));
    }

    /** Xác nhận import — all-or-nothing, chỉ lưu khi tất cả dòng hợp lệ */
    @PreAuthorize("hasAnyRole('TOOLS_STOREKEEPER')")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ToolImportResult> importTools(@RequestParam("file") MultipartFile file) {
        ToolImportResult result = toolExcelService.importTools(file);
        return ApiResponse.success("Đã nhập thành công " + result.getValidCount() + " CCDC", result);
    }
}
