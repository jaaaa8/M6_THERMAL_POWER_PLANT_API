package com.example.m6_thermal_power_plant_api.controller.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.request.AddEquipmentDTO;
import com.example.m6_thermal_power_plant_api.dto.equipment.request.EquipmentUpdateDTO;
import com.example.m6_thermal_power_plant_api.dto.equipment.response.EquipmentAddDTO;
import com.example.m6_thermal_power_plant_api.dto.equipment.response.EquipmentDetailDTO;
import com.example.m6_thermal_power_plant_api.dto.equipment.response.ListEquipmentDTO;
import com.example.m6_thermal_power_plant_api.service.equipment.IEquipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/v1/equipments")
@RequiredArgsConstructor
@CrossOrigin("*")
public class EquipmentController {
    private final IEquipmentService equipmentService;

    @GetMapping
    public ResponseEntity<Page<ListEquipmentDTO>> getEquipmentList(
            @RequestParam (required = false) Integer systemId,
            @RequestParam (required = false) String kks,
            @RequestParam (required = false) String name,
            @RequestParam (required = false) Integer typeId,
            @RequestParam (required = false) String status,
            Pageable pageable
    ){
        return  ResponseEntity.ok(equipmentService.getEquipmentList(
                systemId,
                kks,
                name,
                typeId,
                status ,
                pageable
        ));
    }
    @GetMapping("/system/{systemId}")
    public ResponseEntity<Page<ListEquipmentDTO>> getEquipmentBySystem(
            @PathVariable Integer systemId,
            Pageable pageable){

        return ResponseEntity.ok(
                equipmentService.getBySystem(systemId,pageable)
        );

    }

    @PostMapping(
            value = "/{systemId}/add",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<EquipmentAddDTO> addEquipment(

            @PathVariable Integer systemId,

            @Valid
            @RequestPart("equipment")
            AddEquipmentDTO dto,

            @RequestPart(value = "images", required = false)
            List<MultipartFile> images

    ) throws IOException {

        EquipmentAddDTO response =
                equipmentService.addEquipment(
                        systemId,
                        dto,
                        images
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable (name="id") Integer id ){
        equipmentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{id}")
    public ResponseEntity<EquipmentDetailDTO> getEquipmentDetail(
            @PathVariable Integer id) {

        EquipmentDetailDTO response = equipmentService.getEquipmentDetail(id);

        return ResponseEntity.ok(response);
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<EquipmentDetailDTO> update(
            @PathVariable Integer id,
            @RequestPart("equipment")
            EquipmentUpdateDTO dto,
            @RequestPart(value = "images", required = false)
            List<MultipartFile> images) throws IOException {
        return ResponseEntity.ok(equipmentService.update(id, dto, images));
    }
}
