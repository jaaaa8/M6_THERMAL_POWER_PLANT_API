package com.example.m6_thermal_power_plant_api.service.consumable;

import com.example.m6_thermal_power_plant_api.dto.consumables.ConsumableReceiptCreateDTO;
import com.example.m6_thermal_power_plant_api.dto.consumables.ConsumableReceiptResponseDTO;
import com.example.m6_thermal_power_plant_api.dto.consumables.ConsumableStockDTO;
import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.Consumable;
import com.example.m6_thermal_power_plant_api.entity.ConsumableInventory;
import com.example.m6_thermal_power_plant_api.entity.ConsumableReceipt;
import com.example.m6_thermal_power_plant_api.entity.enums.PartStatus;
import com.example.m6_thermal_power_plant_api.entity.enums.TransactionType;
import com.example.m6_thermal_power_plant_api.repository.AccountRepository;
import com.example.m6_thermal_power_plant_api.repository.IConsumableInventoryRepository;
import com.example.m6_thermal_power_plant_api.repository.IConsumableReceiptRepository;
import com.example.m6_thermal_power_plant_api.repository.IConsumableRepository;
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
public class ConsumableInventoryService implements IConsumableInventoryService {

    private final IConsumableRepository consumableRepository;
    private final IConsumableReceiptRepository receiptRepository;
    private final IConsumableInventoryRepository inventoryRepository;
    private final AccountRepository accountRepository;

    @Override
    public ConsumableReceiptResponseDTO importConsumable(ConsumableReceiptCreateDTO dto, Integer accountId) {
        // 1. Kiểm tra sự tồn tại của vật tư
        Consumable consumable = consumableRepository.findById(dto.getConsumableId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vật tư tiêu hao với ID: " + dto.getConsumableId()));

        if (consumable.getStatus() != PartStatus.ACTIVE) {
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
        ConsumableReceipt receipt = ConsumableReceipt.builder()
                .receiptCode(code)
                .consumable(consumable)
                .quantity(dto.getQuantity())
                .supplier(dto.getSupplier() != null ? dto.getSupplier().trim() : null)
                .receivedBy(account)
                .receivedAt(txDate)
                .build();
        receiptRepository.save(receipt);

        // 4. Ghi sổ nhật ký giao dịch kho (Inventory Ledger)
        ConsumableInventory inventory = ConsumableInventory.builder()
                .consumable(consumable)
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
    public Page<ConsumableStockDTO> searchStock(String code, String name, String manufacturer, PartStatus status, Pageable pageable) {
        return consumableRepository.searchConsumableStock(
                code, name, manufacturer, status, pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConsumableReceiptResponseDTO> getReceiptHistory(Pageable pageable) {
        return receiptRepository.findAllWithDetails(pageable).map(this::toReceiptResponseDTO);
    }

    private ConsumableReceiptResponseDTO toReceiptResponseDTO(ConsumableReceipt r) {
        return ConsumableReceiptResponseDTO.builder()
                .id(r.getId())
                .receiptCode(r.getReceiptCode())
                .consumableId(r.getConsumable() != null ? r.getConsumable().getId() : null)
                .consumableCode(r.getConsumable() != null ? r.getConsumable().getConsumableCode() : null)
                .consumableName(r.getConsumable() != null ? r.getConsumable().getName() : null)
                .unitName(r.getConsumable() != null && r.getConsumable().getUnit() != null ? r.getConsumable().getUnit().getName() : null)
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
