package com.example.m6_thermal_power_plant_api.service.consumable;

import com.example.m6_thermal_power_plant_api.dto.consumables.ConsumableDTO;
import com.example.m6_thermal_power_plant_api.entity.Consumable;
import com.example.m6_thermal_power_plant_api.entity.enums.PartStatus;
import com.example.m6_thermal_power_plant_api.repository.ConsumableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class ConsumableService implements IConsumableService{

    private final ConsumableRepository consumableRepository;


    @Override
    public ConsumableDTO create(ConsumableDTO dto) {
        String code = normalize(dto.getConsumableCode());
        if (consumableRepository.existsByConsumableCode(code)){
            throw new IllegalStateException("Mã vật tư tiêu hao đã tồn tại");
        }

        Consumable consumable = toEntity(dto);
        consumable.setId(null);
        consumable.setConsumableCode(code);
        consumable.setStatus(PartStatus.ACTIVE);
        return toDto(consumableRepository.save(consumable));
    }

    @Override
    public ConsumableDTO update(Integer id, ConsumableDTO dto) {
        Consumable consumable = consumableRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Không tìm thấy vật tư tiêu hao")
        );

        String code = normalize(dto.getConsumableCode());
        if (consumableRepository.existsByConsumableCodeAndIdNot(code, id)){
            throw new IllegalStateException("Mã vật tư tiêu hao đã tồn tại");
        }

        consumable.setConsumableCode(code);
        consumable.setName(dto.getName());
        consumable.setPrice(dto.getPrice());
        consumable.setImgPath(dto.getImgPath());
        consumable.setManufacturer(dto.getManufacturer());
        consumable.setImgPath(dto.getImgPath());
        consumable.setStatus(dto.getStatus());

        return toDto(consumableRepository.save(consumable));
    }

    @Override
    @Transactional(readOnly = true)
    public ConsumableDTO getById(Integer id) {
        return consumableRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(
                () -> new IllegalArgumentException("Không tìm thấy vật tư tiêu hao với id = " + id)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ConsumableDTO getByCode(String code) {
        String normalizeCode = normalize(code);
        return consumableRepository.findByConsumableCodeIgnoreCase(normalizeCode)
                .map(this::toDto)
                .orElseThrow(
                        () -> new IllegalArgumentException("Không tìm thấy vật tư tiêu hao với mã = " + normalizeCode)
                );
    }

    @Override
    public Page<ConsumableDTO> search(String code, String name, String manufacturer, BigDecimal price, PartStatus status, Pageable pageable) {
        return consumableRepository.searchByFields(
                normalize(code),
                normalize(name),
                normalize(manufacturer),
                price,
                status,
                pageable
        ).map(this::toDto);
    }

    @Override
    public void delete(Integer id) {
        Consumable consumable = consumableRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vật tư tiêu hao với id = " + id));

        consumable.setStatus(PartStatus.INACTIVE);
        consumableRepository.save(consumable);
    }

    private String normalize(String value){
        return value == null ? null : value.trim();
    }

    private Consumable toEntity(ConsumableDTO consumableDTO){
        return Consumable.builder()
                .consumableCode(consumableDTO.getConsumableCode())
                .name(consumableDTO.getName())
                .price(consumableDTO.getPrice())
                .manufacturer(consumableDTO.getManufacturer())
                .imgPath(consumableDTO.getImgPath())
                .status(consumableDTO.getStatus() != null ? consumableDTO.getStatus() : PartStatus.ACTIVE)
                .build();
    }

    private ConsumableDTO toDto(Consumable consumable){
        return ConsumableDTO.builder()
                .id(consumable.getId())
                .consumableCode(consumable.getConsumableCode())
                .name(consumable.getName())
                .price(consumable.getPrice())
                .manufacturer(consumable.getManufacturer())
                .imgPath(consumable.getImgPath())
                .build();
    }
}
