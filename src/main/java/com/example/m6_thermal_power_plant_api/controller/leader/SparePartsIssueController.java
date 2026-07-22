package com.example.m6_thermal_power_plant_api.controller.leader;

import com.example.m6_thermal_power_plant_api.dto.Leader.req.SparePartsIssueRequestDto;
import com.example.m6_thermal_power_plant_api.entity.enums.SparePartsIssueStatus;
import com.example.m6_thermal_power_plant_api.service.leader.spare_parts_issue.ISparePartsIssueService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/spare-parts-issue")
public class SparePartsIssueController {
    private final ISparePartsIssueService sparePartsIssueService;
    public SparePartsIssueController(ISparePartsIssueService sparePartsIssueService) {
        this.sparePartsIssueService = sparePartsIssueService;
    }

    @GetMapping("/search")
    public ResponseEntity<Page<SparePartsIssueRequestDto>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) SparePartsIssueStatus status,
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                sparePartsIssueService.search(
                        keyword,
                        status,
                        pageable
                )
        );
    }

    @GetMapping("/add")
    public ResponseEntity<SparePartsIssueRequestDto> addSparePartsIssue() {
        SparePartsIssueRequestDto sparePartsIssueRequestDto = new SparePartsIssueRequestDto();
        return ResponseEntity.ok(sparePartsIssueRequestDto);
    }

    @PostMapping("/add")
    public ResponseEntity<SparePartsIssueRequestDto> saveSparePartsIssue(@RequestBody SparePartsIssueRequestDto sparePartsIssueRequestDto) {
        SparePartsIssueRequestDto savedSparePartsIssue = sparePartsIssueService.save(sparePartsIssueRequestDto);
        return ResponseEntity.ok(savedSparePartsIssue);
    }

    @GetMapping("/update/{id}")
    public ResponseEntity<SparePartsIssueRequestDto> updateSparePartsIssue(@PathVariable("id") Integer id) {
        SparePartsIssueRequestDto sparePartsIssueRequestDto = sparePartsIssueService.findById(id);
        return ResponseEntity.ok(sparePartsIssueRequestDto);
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateSparePartsIssue(@RequestBody SparePartsIssueRequestDto dto) {
        try {
            return ResponseEntity.ok(sparePartsIssueService.update(dto));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(java.util.Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }


    @GetMapping("/detail/{id}")
    public ResponseEntity<SparePartsIssueRequestDto> getSparePartsIssueDetail(@PathVariable("id") Integer id) {
        SparePartsIssueRequestDto sparePartsIssueRequestDto = sparePartsIssueService.findById(id);
        return ResponseEntity.ok(sparePartsIssueRequestDto);
    }

    @PostMapping("/upload-spare-parts-issue")
    public ResponseEntity<?> uploadSparePartsIssuePdf(
            @RequestParam("id") Integer id,
            @RequestParam("pdf") MultipartFile pdf
    ) {
        try {
            SparePartsIssueRequestDto result = sparePartsIssueService.uploadSignedPdf(id, pdf);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Tải lên file PDF thất bại: " + e.getMessage());
        }
    }
}
