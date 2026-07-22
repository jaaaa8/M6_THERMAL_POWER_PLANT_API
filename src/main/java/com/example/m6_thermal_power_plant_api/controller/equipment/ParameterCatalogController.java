package com.example.m6_thermal_power_plant_api.controller.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.request.ParameterCatalogDTO;
import com.example.m6_thermal_power_plant_api.service.equipment.IParameterCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/catalog")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ParameterCatalogController {
    private  final IParameterCatalogService parameterCatalogService;

    @GetMapping
    public ResponseEntity<Page<ParameterCatalogDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(parameterCatalogService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParameterCatalogDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(parameterCatalogService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ParameterCatalogDTO> create(
            @RequestBody ParameterCatalogDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(parameterCatalogService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParameterCatalogDTO> update(
            @PathVariable Integer id,
            @RequestBody ParameterCatalogDTO dto) {

        return ResponseEntity.ok(parameterCatalogService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {

        parameterCatalogService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
