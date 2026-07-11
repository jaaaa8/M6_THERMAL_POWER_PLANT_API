package com.example.m6_thermal_power_plant_api.controller.leader;

import com.example.m6_thermal_power_plant_api.dto.Leader.req.TechnicalAssessmentCreateRequestDto;
import com.example.m6_thermal_power_plant_api.dto.Leader.req.TechnicalAssessmentUpdateRequestDto;
import com.example.m6_thermal_power_plant_api.entity.enums.TechnicalAssessmentStatus;
import com.example.m6_thermal_power_plant_api.service.leader.technical_assessment.ITechnicalAssessmentService;
import com.example.m6_thermal_power_plant_api.service.util.FileUploadService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/technical-assessment")
public class TechnicalAssessmentController {
    private final ITechnicalAssessmentService technicalAssessmentService;
    public  TechnicalAssessmentController(ITechnicalAssessmentService technicalAssessmentService) {
        this.technicalAssessmentService = technicalAssessmentService;
    }
    @GetMapping("/search")
    public ResponseEntity<Page<TechnicalAssessmentUpdateRequestDto>> searchTechnicalAssessments(

            @RequestParam(required = false)
            String technicalCode,

            @RequestParam(required = false)
            Integer equipmentId,

            @RequestParam(required = false)
            TechnicalAssessmentStatus status,

            Pageable pageable
    ) {


        Page<TechnicalAssessmentUpdateRequestDto> result =
                technicalAssessmentService.search(
                        technicalCode,
                        equipmentId,
                        status,
                        pageable
                );


        return ResponseEntity.ok(result);
    }

    @GetMapping("/add")
    public ResponseEntity<TechnicalAssessmentCreateRequestDto> getTechnicalAssessmentForm() {
        TechnicalAssessmentCreateRequestDto form = new TechnicalAssessmentCreateRequestDto();
        return ResponseEntity.ok(form);
    }

    @PostMapping(
            value = "/add",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> submitTechnicalAssessmentForm(
            @Valid
            @RequestPart("data")
            TechnicalAssessmentCreateRequestDto dto,

            @RequestPart(value = "imageFiles", required = false)
            MultipartFile[] imageFiles
    ) {
        try {

            dto.setCreatedAt(LocalDateTime.now());

            TechnicalAssessmentCreateRequestDto result =
                    technicalAssessmentService.save(dto, imageFiles);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @GetMapping("/{technicalCode}")
    public ResponseEntity<TechnicalAssessmentUpdateRequestDto> getEditTechnicalAssessmentForm(@PathVariable("technicalCode") String technicalCode) {
        TechnicalAssessmentUpdateRequestDto existingAssessment = technicalAssessmentService.findByTechnicalCode(technicalCode);
        if (existingAssessment == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(existingAssessment);
    }

    @PostMapping(value = "/edit",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> edit(
            @RequestParam("id") Integer id,
            @RequestPart(value = "pdfFile", required = false)
            MultipartFile pdfFile) {

        try {

            TechnicalAssessmentUpdateRequestDto dto =
                    technicalAssessmentService.findById(id);

            if (dto.getStatus() == TechnicalAssessmentStatus.COMPLETED) {
                throw new IllegalStateException(
                        "Biên bản đã hoàn thành, không thể cập nhật PDF."
                );
            }

            return ResponseEntity.ok(
                    technicalAssessmentService.update(dto, pdfFile)
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @GetMapping("/delete-pdf/{id}")
    public ResponseEntity<?> deletePdf(@PathVariable("id") Integer id) {
        try {

            TechnicalAssessmentUpdateRequestDto dto =
                    technicalAssessmentService.findById(id);
            if(dto.getStatus() == TechnicalAssessmentStatus.PENDING) {
                throw new IllegalStateException(
                        "Biên bản chưa có PDF, không thể xoá."
                );
            }
            return ResponseEntity.ok(technicalAssessmentService.deletePdfAttachment(dto));

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}
