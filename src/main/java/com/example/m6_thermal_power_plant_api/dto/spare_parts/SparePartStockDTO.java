package com.example.m6_thermal_power_plant_api.dto.spare_parts;

import com.example.m6_thermal_power_plant_api.entity.enums.PartStatus;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
public class SparePartStockDTO {
    private Integer id;
    private String sparePartCode;
    private String name;
    private BigDecimal price;
    private String manufacturer;
    private String imgPath;
    private Integer unitId;
    private String unitName;
    private PartStatus status;
    private BigDecimal currentStock;

    @Builder
    public SparePartStockDTO(Integer id, String sparePartCode, String name, BigDecimal price, String manufacturer, String imgPath, Integer unitId, String unitName, PartStatus status, BigDecimal currentStock) {
        this.id = id;
        this.sparePartCode = sparePartCode;
        this.name = name;
        this.price = price;
        this.manufacturer = manufacturer;
        this.imgPath = imgPath;
        this.unitId = unitId;
        this.unitName = unitName;
        this.status = status;
        this.currentStock = currentStock;
    }

    public SparePartStockDTO(Integer id, String sparePartCode, String name, BigDecimal price, String manufacturer, String imgPath, Integer unitId, String unitName, PartStatus status, Double currentStock) {
        this.id = id;
        this.sparePartCode = sparePartCode;
        this.name = name;
        this.price = price;
        this.manufacturer = manufacturer;
        this.imgPath = imgPath;
        this.unitId = unitId;
        this.unitName = unitName;
        this.status = status;
        this.currentStock = currentStock != null ? BigDecimal.valueOf(currentStock) : BigDecimal.ZERO;
    }

    public SparePartStockDTO(Integer id, String sparePartCode, String name, BigDecimal price, String manufacturer, String imgPath, Integer unitId, String unitName, PartStatus status, Long currentStock) {
        this.id = id;
        this.sparePartCode = sparePartCode;
        this.name = name;
        this.price = price;
        this.manufacturer = manufacturer;
        this.imgPath = imgPath;
        this.unitId = unitId;
        this.unitName = unitName;
        this.status = status;
        this.currentStock = currentStock != null ? BigDecimal.valueOf(currentStock) : BigDecimal.ZERO;
    }

    public SparePartStockDTO(Integer id, String sparePartCode, String name, BigDecimal price, String manufacturer, String imgPath, Integer unitId, String unitName, PartStatus status, Integer currentStock) {
        this.id = id;
        this.sparePartCode = sparePartCode;
        this.name = name;
        this.price = price;
        this.manufacturer = manufacturer;
        this.imgPath = imgPath;
        this.unitId = unitId;
        this.unitName = unitName;
        this.status = status;
        this.currentStock = currentStock != null ? BigDecimal.valueOf(currentStock) : BigDecimal.ZERO;
    }

    public SparePartStockDTO(Integer id, String sparePartCode, String name, BigDecimal price, String manufacturer, String imgPath, Integer unitId, String unitName, PartStatus status, int currentStock) {
        this.id = id;
        this.sparePartCode = sparePartCode;
        this.name = name;
        this.price = price;
        this.manufacturer = manufacturer;
        this.imgPath = imgPath;
        this.unitId = unitId;
        this.unitName = unitName;
        this.status = status;
        this.currentStock = BigDecimal.valueOf(currentStock);
    }

    public SparePartStockDTO(Integer id, String sparePartCode, String name, BigDecimal price, String manufacturer, String imgPath, Integer unitId, String unitName, PartStatus status, double currentStock) {
        this.id = id;
        this.sparePartCode = sparePartCode;
        this.name = name;
        this.price = price;
        this.manufacturer = manufacturer;
        this.imgPath = imgPath;
        this.unitId = unitId;
        this.unitName = unitName;
        this.status = status;
        this.currentStock = BigDecimal.valueOf(currentStock);
    }

    public SparePartStockDTO(Integer id, String sparePartCode, String name, BigDecimal price, String manufacturer, String imgPath, Integer unitId, String unitName, PartStatus status, long currentStock) {
        this.id = id;
        this.sparePartCode = sparePartCode;
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
