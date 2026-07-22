package com.example.m6_thermal_power_plant_api.service.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.request.AddEquipmentDTO;
import com.example.m6_thermal_power_plant_api.dto.equipment.request.EquipmentUpdateDTO;
import com.example.m6_thermal_power_plant_api.dto.equipment.response.EquipmentAddDTO;
import com.example.m6_thermal_power_plant_api.dto.equipment.response.EquipmentDetailDTO;
import com.example.m6_thermal_power_plant_api.dto.equipment.response.ListEquipmentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IEquipmentService {
    Page<ListEquipmentDTO> getEquipmentList(Integer systemId,String kks, String name, Integer typeId, String status, Pageable pageable);

    Page<ListEquipmentDTO> getBySystem(Integer systemId, Pageable pageable);

    EquipmentAddDTO addEquipment(Integer systemId, AddEquipmentDTO dto, List<MultipartFile> images) throws IOException;
    EquipmentDetailDTO getEquipmentDetail(Integer id);

    EquipmentDetailDTO update(
            Integer id,
            EquipmentUpdateDTO dto,List<MultipartFile> images
    ) throws IOException;
    void deleteById(Integer id);
}

