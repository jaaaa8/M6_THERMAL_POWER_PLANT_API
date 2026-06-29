package com.example.m6_thermal_power_plant_api.controller.leader;

import com.example.m6_thermal_power_plant_api.dto.Leader.req.TechnicalAssessmentCreateRequestDto;
import com.example.m6_thermal_power_plant_api.dto.Leader.req.TechnicalAssessmentUpdateRequestDto;
import com.example.m6_thermal_power_plant_api.dto.Leader.res.TechnicalAssessmentResponseDto;
import com.example.m6_thermal_power_plant_api.service.leader.technical_assessment.ITechnicalAssessmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*")
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
    public ResponseEntity<TechnicalAssessmentCreateRequestDto> getTechnicalAssessmentForm() {
        TechnicalAssessmentCreateRequestDto form = new TechnicalAssessmentCreateRequestDto();
        return ResponseEntity.ok(form);
    }

    @PostMapping("/add")
    public ResponseEntity<TechnicalAssessmentCreateRequestDto> submitTechnicalAssessmentForm(@RequestBody TechnicalAssessmentCreateRequestDto dto) {
        try {
            dto.setCreatedAt(LocalDateTime.now());
            TechnicalAssessmentCreateRequestDto requestDto = technicalAssessmentService.save(dto);
            return ResponseEntity.created(null).body(requestDto);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/edit/{technicalCode}")
    public ResponseEntity<TechnicalAssessmentUpdateRequestDto> getEditTechnicalAssessmentForm(@PathVariable("technicalCode") String technicalCode) {
        TechnicalAssessmentUpdateRequestDto existingAssessment = technicalAssessmentService.findByTechnicalCode(technicalCode);
        if (existingAssessment == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(existingAssessment);
    }

    @PostMapping(value = "/edit",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TechnicalAssessmentUpdateRequestDto> edit(
            @RequestParam("id") Integer id,
            @RequestPart(value = "pdfFile", required = false)
            MultipartFile pdfFile) {
        try {
            TechnicalAssessmentUpdateRequestDto dto = technicalAssessmentService.findById(id);
            return ResponseEntity.ok(
                    technicalAssessmentService.update(dto, pdfFile)
            );
        }catch (Exception e){
            return ResponseEntity.badRequest().body(null);
        }
    }
}
