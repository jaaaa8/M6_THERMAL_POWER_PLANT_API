package com.example.m6_thermal_power_plant_api.service.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.response.SystemListDTO;
import com.example.m6_thermal_power_plant_api.entity.EquipmentSystem;
import com.example.m6_thermal_power_plant_api.entity.enums.EquipmentStatus;
import com.example.m6_thermal_power_plant_api.exception.ObjectNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.IEquipmentSystemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentSystemService  implements IEquipmentSystemService{

    private final IEquipmentSystemRepository equipmentSystemRepository;
    @Override
    public Page<SystemListDTO> getSystem(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page,size);
        Page<EquipmentSystem> result;
        if(keyword == null || keyword.isBlank()){
            result = equipmentSystemRepository.findAll(pageable);
        }else{
            result = equipmentSystemRepository.findByNameContainingIgnoreCase(
                    keyword,pageable
            );
        }
        return  result.map(this :: convertDTO);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        EquipmentSystem system= equipmentSystemRepository.findById(id).orElseThrow(()-> new ObjectNotFoundException("Không tìm thấy hệ thống."));

        system.setStatus(EquipmentStatus.RETIRED);
        system.setIsDeleted(true);
        equipmentSystemRepository.save(system);

    }

    private SystemListDTO convertDTO(EquipmentSystem equipmentSystem) {
       return SystemListDTO.builder()
               .code(equipmentSystem.getCode())
               .name(equipmentSystem.getName())
               .status(equipmentSystem.getStatus())
               .build();
    }
}
