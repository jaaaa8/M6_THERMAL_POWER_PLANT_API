package com.example.m6_thermal_power_plant_api.controller.leader;

import com.example.m6_thermal_power_plant_api.dto.Leader.req.TechnicalAssessmentRequestDto;
import com.example.m6_thermal_power_plant_api.dto.Leader.res.TechnicalAssessmentResponseDto;
import com.example.m6_thermal_power_plant_api.service.leader.technical_assessment.ITechnicalAssessmentService;
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
    @GetMapping("")
    public ResponseEntity<List<TechnicalAssessmentResponseDto>> getTechnicalAssessments() {
        List<TechnicalAssessmentResponseDto> technicalAssessments = technicalAssessmentService.findAll();
        return ResponseEntity.ok(technicalAssessments);
    }

    @GetMapping("/add")
    public ResponseEntity<TechnicalAssessmentRequestDto> getTechnicalAssessmentForm() {
        TechnicalAssessmentRequestDto form = new TechnicalAssessmentRequestDto();
        return ResponseEntity.ok(form);
    }

    @PostMapping("/add")
    public ResponseEntity<TechnicalAssessmentRequestDto> submitTechnicalAssessmentForm(TechnicalAssessmentRequestDto dto) {
        try {
            dto.setCreatedAt(LocalDateTime.now());
            TechnicalAssessmentRequestDto requestDto = technicalAssessmentService.save(dto);
            return ResponseEntity.created(null).body(requestDto);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/edit/{technicalCode}")
    public ResponseEntity<TechnicalAssessmentRequestDto> getEditTechnicalAssessmentForm(@PathVariable("technicalCode") String technicalCode) {
        TechnicalAssessmentResponseDto existingAssessment = technicalAssessmentService.findByTechnicalCode(technicalCode);
        if (existingAssessment == null) {
            return ResponseEntity.notFound().build();
        }
        TechnicalAssessmentRequestDto dto = new TechnicalAssessmentRequestDto();
        dto.setTechnicalCode(existingAssessment.getTechnicalCode());
        dto.setAssessor(existingAssessment.getAssessor());
        dto.setAttachmentPath(existingAssessment.getAttachmentPath());
        dto.setImgPath(existingAssessment.getImgPath());
        dto.setResult(existingAssessment.getResult());
        dto.setDescription(existingAssessment.getDescription());
        dto.setCreatedAt(existingAssessment.getCreatedAt());
        return ResponseEntity.ok(dto);
    }

    @PostMapping(value = "/edit",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TechnicalAssessmentRequestDto> edit(
            @RequestPart("dto") TechnicalAssessmentRequestDto dto,
            @RequestPart(value = "pdfFile", required = false)
            MultipartFile pdfFile) {
        try {
            return ResponseEntity.ok(
                    technicalAssessmentService.update(dto, pdfFile)
            );
        }catch (Exception e){
            return ResponseEntity.badRequest().body(null);
        }
    }
}
