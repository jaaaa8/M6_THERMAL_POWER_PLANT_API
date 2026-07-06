package com.example.m6_thermal_power_plant_api.dto.consumables;

import com.example.m6_thermal_power_plant_api.entity.enums.PartStatus;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ConsumableStockDTO {
    private Integer id;
    private String consumableCode;
    private String name;
    private BigDecimal price;
    private String manufacturer;
    private String imgPath;
    private Integer unitId;
    private String unitName;
    private PartStatus status;
    private BigDecimal currentStock;

    @Builder
    public ConsumableStockDTO(Integer id, String consumableCode, String name, BigDecimal price, String manufacturer, String imgPath, Integer unitId, String unitName, PartStatus status, BigDecimal currentStock) {
        this.id = id;
        this.consumableCode = consumableCode;
        this.name = name;
        this.price = price;
        this.manufacturer = manufacturer;
        this.imgPath = imgPath;
        this.unitId = unitId;
        this.unitName = unitName;
        this.status = status;
        this.currentStock = currentStock;
    }

    public ConsumableStockDTO(Integer id, String consumableCode, String name, BigDecimal price, String manufacturer, String imgPath, Integer unitId, String unitName, PartStatus status, Double currentStock) {
        this.id = id;
        this.consumableCode = consumableCode;
        this.name = name;
        this.price = price;
        this.manufacturer = manufacturer;
        this.imgPath = imgPath;
        this.unitId = unitId;
        this.unitName = unitName;
        this.status = status;
        this.currentStock = currentStock != null ? BigDecimal.valueOf(currentStock) : BigDecimal.ZERO;
    }

    public ConsumableStockDTO(Integer id, String consumableCode, String name, BigDecimal price, String manufacturer, String imgPath, Integer unitId, String unitName, PartStatus status, Long currentStock) {
        this.id = id;
        this.consumableCode = consumableCode;
        this.name = name;
        this.price = price;
        this.manufacturer = manufacturer;
        this.imgPath = imgPath;
        this.unitId = unitId;
        this.unitName = unitName;
        this.status = status;
        this.currentStock = currentStock != null ? BigDecimal.valueOf(currentStock) : BigDecimal.ZERO;
    }

    public ConsumableStockDTO(Integer id, String consumableCode, String name, BigDecimal price, String manufacturer, String imgPath, Integer unitId, String unitName, PartStatus status, Integer currentStock) {
        this.id = id;
        this.consumableCode = consumableCode;
        this.name = name;
        this.price = price;
        this.manufacturer = manufacturer;
        this.imgPath = imgPath;
        this.unitId = unitId;
        this.unitName = unitName;
        this.status = status;
        this.currentStock = currentStock != null ? BigDecimal.valueOf(currentStock) : BigDecimal.ZERO;
    }

    public ConsumableStockDTO(Integer id, String consumableCode, String name, BigDecimal price, String manufacturer, String imgPath, Integer unitId, String unitName, PartStatus status, int currentStock) {
        this.id = id;
        this.consumableCode = consumableCode;
        this.name = name;
        this.price = price;
        this.manufacturer = manufacturer;
        this.imgPath = imgPath;
        this.unitId = unitId;
        this.unitName = unitName;
        this.status = status;
        this.currentStock = BigDecimal.valueOf(currentStock);
    }

    public ConsumableStockDTO(Integer id, String consumableCode, String name, BigDecimal price, String manufacturer, String imgPath, Integer unitId, String unitName, PartStatus status, double currentStock) {
        this.id = id;
        this.consumableCode = consumableCode;
        this.name = name;
        this.price = price;
        this.manufacturer = manufacturer;
        this.imgPath = imgPath;
        this.unitId = unitId;
        this.unitName = unitName;
        this.status = status;
        this.currentStock = BigDecimal.valueOf(currentStock);
    }

    public ConsumableStockDTO(Integer id, String consumableCode, String name, BigDecimal price, String manufacturer, String imgPath, Integer unitId, String unitName, PartStatus status, long currentStock) {
        this.id = id;
        this.consumableCode = consumableCode;
        this.name = name;
        this.price = price;
        this.manufacturer = manufacturer;
        this.imgPath = imgPath;
        this.unitId = unitId;
        this.unitName = unitName;
        this.status = status;
        this.currentStock = BigDecimal.valueOf(currentStock);
    }
}
