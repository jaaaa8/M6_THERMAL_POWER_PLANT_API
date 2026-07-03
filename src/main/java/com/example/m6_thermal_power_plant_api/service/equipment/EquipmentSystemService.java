package com.example.m6_thermal_power_plant_api.service.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.request.CreateSystemDTO;
import com.example.m6_thermal_power_plant_api.dto.equipment.request.UpdateSystemDTO;
import com.example.m6_thermal_power_plant_api.dto.equipment.response.SystemListDTO;
import com.example.m6_thermal_power_plant_api.entity.EquipmentSystem;
import com.example.m6_thermal_power_plant_api.entity.enums.EquipmentStatus;
import com.example.m6_thermal_power_plant_api.exception.ObjectNotFoundException;
import com.example.m6_thermal_power_plant_api.repository.equipment.IEquipmentSystemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentSystemService  implements IEquipmentSystemService{

    private final IEquipmentSystemRepository equipmentSystemRepository;
    @Override
    public Page<SystemListDTO> getSystem(String name,EquipmentStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page,size);
        Page<EquipmentSystem> result = equipmentSystemRepository.search(
                (name == null || name.isBlank()) ? null : name,
                status,
                pageable
        );
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

    @Override
    public SystemListDTO getById(int id) {
        EquipmentSystem system= equipmentSystemRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy hệ thống"));
        return convertDTO(system);
    }

    @Override
    public SystemListDTO createSystem(CreateSystemDTO dto) {
        if(equipmentSystemRepository.existsByNameIgnoreCase(dto.getName())){
            throw new RuntimeException("Hệ thống đã tồn tại !!!");
        }
        String code = generateCode();

        EquipmentSystem system= EquipmentSystem.builder()
                .code(code)
                .name(dto.getName())
                .description(dto.getDescription())
                .status(EquipmentStatus.ACTIVE)
                .build();
        equipmentSystemRepository.save(system);
        return convertDTO(system);
    }

    @Override
    @Transactional
    public SystemListDTO updateSystem(int id, UpdateSystemDTO dto) {
        EquipmentSystem system =equipmentSystemRepository.findById(id)
                .orElseThrow(()-> new ObjectNotFoundException("Không tìm thấy hệ thống."));
        if(equipmentSystemRepository.existsByNameIgnoreCaseAndIdNot(dto.getName(),id)){
            throw  new RuntimeException("Tên hệ thống đã tồn tại.");
        }
        system.setName(dto.getName().trim());
        system.setDescription(dto.getDescription());
        system.setStatus(dto.getStatus());
        equipmentSystemRepository.save(system);
        return convertDTO(system);

    }

    private SystemListDTO convertDTO(EquipmentSystem equipmentSystem) {
       return SystemListDTO.builder()
               .id(equipmentSystem.getId())
               .code(equipmentSystem.getCode())
               .name(equipmentSystem.getName())
               .status(equipmentSystem.getStatus())
               .description(equipmentSystem.getDescription())
               .build();
    }

    private String generateCode() {

        List<EquipmentSystem> systems = equipmentSystemRepository.findByCodeStartingWith("SYS");

        int max = 0;

        for (EquipmentSystem system : systems) {
            String code = system.getCode(); // SYS001

            int number = Integer.parseInt(code.substring(4));

            max = Math.max(max, number);
        }

        return "SYS-" + String.format("%03d", max + 1);
    }
}
