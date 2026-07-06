package com.example.m6_thermal_power_plant_api.service.spare_part;


import com.example.m6_thermal_power_plant_api.dto.spare_parts.SparePartReceiptCreateDTO;
import com.example.m6_thermal_power_plant_api.dto.spare_parts.SparePartReceiptResponseDTO;
import com.example.m6_thermal_power_plant_api.dto.spare_parts.SparePartStockDTO;
import com.example.m6_thermal_power_plant_api.entity.*;
import com.example.m6_thermal_power_plant_api.entity.enums.PartStatus;
import com.example.m6_thermal_power_plant_api.entity.enums.TransactionType;
import com.example.m6_thermal_power_plant_api.repository.*;
import com.example.m6_thermal_power_plant_api.util.TimeStampCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class SparePartInventoryService implements ISparePartInventoryService {

    private final ISparePartRepository sparePartRepository;
    private final ISparePartReceiptRepository receiptRepository;
    private final ISparePartInventoryRepository inventoryRepository;
    private final AccountRepository accountRepository;

    @Override
    public SparePartReceiptResponseDTO importSparePart(SparePartReceiptCreateDTO dto, Integer accountId) {
        // 1. Kiểm tra sự tồn tại của vật tư
        SparePart sparePart = sparePartRepository.findById(dto.getSparePartId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vật tư thay thế với ID: " + dto.getSparePartId()));

        if (sparePart.getStatus() != PartStatus.ACTIVE) {
            throw new IllegalStateException("Không thể nhập kho loại vật tư đã ngừng hoạt động.");
        }

        // 2. Tìm tài khoản thực hiện
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản thủ kho với ID: " + accountId));

        LocalDateTime txDate = dto.getReceivedAt() != null ? dto.getReceivedAt() : LocalDateTime.now();

        // 3. Tạo hóa đơn/phiếu nhập kho (Receipt)
        String code = normalize(dto.getReceiptCode());
        if(code == null || code.isBlank()){
            code = TimeStampCodeGenerator.generate(ConsumableInventory.class);
        }
        SparePartReceipt receipt = SparePartReceipt.builder()
                .receiptCode(code)
                .sparePart(sparePart)
                .quantity(dto.getQuantity())
                .supplier(dto.getSupplier() != null ? dto.getSupplier().trim() : null)
                .receivedBy(account)
                .receivedAt(txDate)
                .build();
        receiptRepository.save(receipt);

        // 4. Ghi sổ nhật ký giao dịch kho (Inventory Ledger)
        SparePartsInventory inventory = SparePartsInventory.builder()
                .sparePart(sparePart)
                .supplier(dto.getSupplier() != null ? dto.getSupplier().trim() : null)
                .account(account)
                .quantity(dto.getQuantity())
                .transactionType(TransactionType.IMPORT)
                .transactionDate(txDate)
                .build();
        inventoryRepository.save(inventory);

        return toReceiptResponseDTO(receipt);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SparePartStockDTO> searchStock(String code, String name, String manufacturer, PartStatus status, Pageable pageable) {
        return sparePartRepository.searchSparePartStock(
                code, name, manufacturer, status, pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SparePartReceiptResponseDTO> getReceiptHistory(Pageable pageable) {
        return receiptRepository.findAllWithDetails(pageable).map(this::toReceiptResponseDTO);
    }

    private SparePartReceiptResponseDTO toReceiptResponseDTO(SparePartReceipt r) {
        return SparePartReceiptResponseDTO.builder()
                .id(r.getId())
                .receiptCode(r.getReceiptCode())
                .sparePartId(r.getSparePart() != null ? r.getSparePart().getId() : null)
                .sparePartCode(r.getSparePart() != null ? r.getSparePart().getSparePartCode() : null)
                .sparePartName(r.getSparePart() != null ? r.getSparePart().getName() : null)
                .unitName(r.getSparePart() != null && r.getSparePart().getUnit() != null ? r.getSparePart().getUnit().getName() : null)
                .quantity(r.getQuantity())
                .supplier(r.getSupplier())
                .receivedByUsername(r.getReceivedBy() != null ? r.getReceivedBy().getUsername() : null)
                .receivedAt(r.getReceivedAt())
                .build();
    }
    private String normalize(String value){
        return value == null ? null : value.trim();
    }

}
