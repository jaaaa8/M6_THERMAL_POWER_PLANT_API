package com.example.m6_thermal_power_plant_api.controller.leader;

import com.example.m6_thermal_power_plant_api.dto.consumables.ConsumableIssueDTO;
import com.example.m6_thermal_power_plant_api.entity.enums.ConsumableIssueStatus;
import com.example.m6_thermal_power_plant_api.service.consumable.IConsumableIssuesService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/consumable-issues")
public class ConsumableIssueController {

    private final IConsumableIssuesService consumableIssuesService;

    public ConsumableIssueController(IConsumableIssuesService consumableIssuesService) {
        this.consumableIssuesService = consumableIssuesService;
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ConsumableIssueDTO>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ConsumableIssueStatus status,
            Pageable pageable
    ) {
        return ResponseEntity.ok(consumableIssuesService.search(keyword, status, pageable));
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<ConsumableIssueDTO> getDetail(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(consumableIssuesService.findById(id));
    }

    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody ConsumableIssueDTO dto) {
        try {
            return ResponseEntity.ok(consumableIssuesService.update(dto));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(java.util.Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/upload-consumable-issue")
    public ResponseEntity<?> uploadSignedPdf(
            @RequestParam("id") Integer id,
            @RequestParam("pdf") MultipartFile pdf
    ) {
        try {
            ConsumableIssueDTO result = consumableIssuesService.uploadSignedPdf(id, pdf);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Tải lên file PDF thất bại: " + e.getMessage());
        }
    }
}
