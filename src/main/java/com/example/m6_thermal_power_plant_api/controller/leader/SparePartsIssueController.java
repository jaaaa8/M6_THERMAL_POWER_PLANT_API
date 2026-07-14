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
    public ResponseEntity<SparePartsIssueRequestDto> updateSparePartsIssue(@RequestBody SparePartsIssueRequestDto sparePartsIssueRequestDto) {
        SparePartsIssueRequestDto updatedSparePartsIssue = sparePartsIssueService.update(sparePartsIssueRequestDto);
        return ResponseEntity.ok(updatedSparePartsIssue);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<SparePartsIssueRequestDto> getSparePartsIssueDetail(@PathVariable("id") Integer id) {
        SparePartsIssueRequestDto sparePartsIssueRequestDto = sparePartsIssueService.findById(id);
        return ResponseEntity.ok(sparePartsIssueRequestDto);
    }
}
