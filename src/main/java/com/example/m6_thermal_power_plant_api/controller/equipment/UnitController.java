package com.example.m6_thermal_power_plant_api.controller.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.request.UnitDTO;
import com.example.m6_thermal_power_plant_api.dto.equipment.response.UnitListDTO;
import com.example.m6_thermal_power_plant_api.service.equipment.IUnitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/units")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UnitController {
    private  final IUnitService unitService;

    @GetMapping("/{id}")
    public ResponseEntity<UnitListDTO> getById(
            @PathVariable (name= "id") int id
    ){
        return  ResponseEntity.ok(unitService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<UnitListDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(unitService.getAll(pageable));
    }

    @PostMapping
    public ResponseEntity<UnitListDTO> createUnit( @Valid @RequestBody UnitDTO dto){
        UnitListDTO unit = unitService.createUnit(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(unit);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUnit(@PathVariable (name="id") Integer id){
        unitService.deleteUnit(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}")
    public ResponseEntity<UnitListDTO> updateUnit(@Valid @PathVariable (name="id") Integer id,
                                                  @RequestBody UnitDTO dto){
        return ResponseEntity.ok(unitService.updateUnit(id,dto));
    }
}
