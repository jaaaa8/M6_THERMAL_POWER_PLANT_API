package com.example.m6_thermal_power_plant_api.service.spare_part;

import com.example.m6_thermal_power_plant_api.dto.spare_parts.SparePartDTO;
import com.example.m6_thermal_power_plant_api.entity.SparePart;
import com.example.m6_thermal_power_plant_api.entity.enums.PartStatus;
import com.example.m6_thermal_power_plant_api.repository.SparePartRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class SparePartService implements ISparePartService{

    private final SparePartRepository sparePartRepository;

    @Override
    public SparePartDTO create(SparePartDTO dto) {
        String code = normalize(dto.getSparePartCode());
        if (sparePartRepository.existsBySparePartCode(code)) {
            throw new IllegalStateException("Mã vật tư thay thế đã tồn tại");
        }

        SparePart sparePart = toEntity(dto);
        sparePart.setId(null);
        sparePart.setSparePartCode(code);
        sparePart.setStatus(PartStatus.ACTIVE);

        return toDto(sparePartRepository.save(sparePart));
    }

    @Override
    public SparePartDTO update(Integer id, SparePartDTO dto) {
        SparePart sparePart = sparePartRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vật tư thay thế với id = " + id));

        String code = normalize(dto.getSparePartCode());
        if (sparePartRepository.existsBySparePartCodeAndIdNot(code, id)) {
            throw new IllegalStateException("Mã vật tư thay thế đã tồn tại");
        }

        sparePart.setSparePartCode(code);
        sparePart.setName(normalize(dto.getName()));
        sparePart.setPrice(dto.getPrice());
        sparePart.setManufacturer(normalize(dto.getManufacturer()));
        sparePart.setImgPath(normalize(dto.getImgPath()));

        return toDto(sparePartRepository.save(sparePart));
    }

    @Override
    @Transactional(readOnly = true)
    public SparePartDTO getById(Integer id) {
        return sparePartRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vật tư thay thế với id = " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public SparePartDTO getByCode(String code) {
        String normalizedCode = normalize(code);
        return sparePartRepository.findBySparePartCodeIgnoreCase(normalizedCode)
                .map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vật tư thay thế với mã = " + code));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SparePartDTO> search(
            String code,
            String name,
            String manufacturer,
            BigDecimal price,
            PartStatus status,
            Pageable pageable
    ) {
        return sparePartRepository.searchByFields(
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
        SparePart sparePart = sparePartRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vật tư thay thế với id = " + id));

        sparePart.setStatus(PartStatus.INACTIVE);
        sparePartRepository.save(sparePart);
    }

    public String normalize(String value){
        return value == null ? null : value.trim();
    }

    public SparePart toEntity(SparePartDTO dto){
        return SparePart.builder()
                .sparePartCode(normalize(dto.getSparePartCode()))
                .name(normalize(dto.getName()))
                .price(dto.getPrice())
                .manufacturer(normalize(dto.getManufacturer()))
                .imgPath(normalize(dto.getImgPath()))
                .status(PartStatus.ACTIVE)
                .build();
    }

    private SparePartDTO toDto(SparePart entity) {
        return SparePartDTO.builder()
                .id(entity.getId())
                .sparePartCode(entity.getSparePartCode())
                .name(entity.getName())
                .price(entity.getPrice())
                .manufacturer(entity.getManufacturer())
                .imgPath(entity.getImgPath())
                .build();
    }
}
