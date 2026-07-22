package com.example.m6_thermal_power_plant_api.controller.leader;

import com.example.m6_thermal_power_plant_api.dto.Leader.req.SparePartsIssueRequestDto;
import com.example.m6_thermal_power_plant_api.service.leader.spare_parts_issue.ISparePartsIssueService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("")
    public ResponseEntity<List<SparePartsIssueRequestDto>> getAllSparePartsIssues() {
        List<SparePartsIssueRequestDto> sparePartsIssues = sparePartsIssueService.findAll();
        return ResponseEntity.ok(sparePartsIssues);
    }

    @GetMapping("/add")
    public ResponseEntity<SparePartsIssueRequestDto> addSparePartsIssue() {
        SparePartsIssueRequestDto sparePartsIssueRequestDto = new SparePartsIssueRequestDto();
        return ResponseEntity.ok(sparePartsIssueRequestDto);
    }

    @PreAuthorize("hasAnyRole('TEAM_LEADER')")
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

    @PreAuthorize("hasAnyRole('TEAM_LEADER')")
    @PostMapping("/update")
    public ResponseEntity<SparePartsIssueRequestDto> updateSparePartsIssue(@RequestBody SparePartsIssueRequestDto sparePartsIssueRequestDto) {
        SparePartsIssueRequestDto updatedSparePartsIssue = sparePartsIssueService.update(sparePartsIssueRequestDto);
        return ResponseEntity.ok(updatedSparePartsIssue);
    }

    @PreAuthorize("hasAnyRole('TEAM_LEADER')")
    @PostMapping("/upload-spare-parts-issue")
    public ResponseEntity<SparePartsIssueRequestDto> uploadSparePartsIssue(@RequestParam("id") Integer id,
                                                                           @RequestPart(value = "pdf", required = false) MultipartFile[] pdf) {
        try {
            SparePartsIssueRequestDto updatedSparePartsIssue = sparePartsIssueService.upload(id, pdf);
            return ResponseEntity.ok(updatedSparePartsIssue);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<SparePartsIssueRequestDto> getSparePartsIssueDetail(@PathVariable("id") Integer id) {
        SparePartsIssueRequestDto sparePartsIssueRequestDto = sparePartsIssueService.findById(id);
        return ResponseEntity.ok(sparePartsIssueRequestDto);
    }
}
