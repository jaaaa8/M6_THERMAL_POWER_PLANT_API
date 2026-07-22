package com.example.m6_thermal_power_plant_api.controller.tool;

import com.example.m6_thermal_power_plant_api.dto.file.FileUploadResult;
import com.example.m6_thermal_power_plant_api.service.util.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Upload ảnh CCDC lên Cloudinary — trả về URL để client gắn vào
 * {@code ToolRequest.imgPath} khi tạo/sửa CCDC (không tự lưu DB ở đây,
 * việc lưu do {@code ToolController} đảm nhiệm qua create/update).
 *
 * Cùng pattern với {@code ConsumableImageUploadController}.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/tools/images")
@RequiredArgsConstructor
public class ToolImageUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            FileUploadResult result = fileUploadService.uploadImage(file);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("url", result.secureUrl()));
        } catch (IOException e) {
            log.warn("Upload anh CCDC that bai.", e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("message", "Upload anh len Cloudinary that bai, vui long thu lai."));
        }
    }
}
