package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.dto.spare_parts.SparePartStockDTO;
import com.example.m6_thermal_power_plant_api.entity.SparePart;
import com.example.m6_thermal_power_plant_api.entity.enums.PartStatus;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ISparePartRepository extends JpaRepository<SparePart, Integer>, JpaSpecificationExecutor<SparePart> {
    boolean existsBySparePartCode(String sparePartCode);
    boolean existsBySparePartCodeAndIdNot(String sparePartCode, Integer id);

    Optional<SparePart> findBySparePartCodeIgnoreCase(String sparePartCode);


    @Query("""
    select s
    from SparePart s
    where (:code is null or :code = '' or lower(s.sparePartCode) like lower(concat('%', :code, '%')))
      and (:name is null or :name = '' or lower(s.name) like lower(concat('%', :name, '%')))
      and (:manufacturer is null or :manufacturer = '' or lower(s.manufacturer) like lower(concat('%', :manufacturer, '%')))
      and (:price is null or s.price = :price)
      and (:status is null or s.status = :status)
    """)
    Page<SparePart> searchByFields(
            @Param("code") String code,
            @Param("name") String name,
            @Param("manufacturer") String manufacturer,
            @Param("price") BigDecimal price,
            @Param("status") PartStatus status,
            Pageable pageable
    );

    @Query("""
        select new com.example.m6_thermal_power_plant_api.dto.spare_parts.SparePartStockDTO(
            c.id, c.sparePartCode, c.name, c.price, c.manufacturer, c.imgPath,
            u.id, u.name, c.status,
            coalesce((select sum(spi.quantity) from SparePartsInventory spi where spi.sparePart.id = c.id and spi.transactionType = com.example.m6_thermal_power_plant_api.entity.enums.TransactionType.IMPORT and spi.isDeleted = false), 0) -
            coalesce((select sum(spi.quantity) from SparePartsInventory spi where spi.sparePart.id = c.id and spi.transactionType = com.example.m6_thermal_power_plant_api.entity.enums.TransactionType.EXPORT and spi.isDeleted = false), 0)
        )
        from SparePart c
        left join c.unit u
        where (:code is null or :code = '' or lower(c.sparePartCode) like lower(concat('%', :code, '%')))
          and (:name is null or :name = '' or lower(c.name) like lower(concat('%', :name, '%')))
          and (:manufacturer is null or :manufacturer = '' or lower(c.manufacturer) like lower(concat('%', :manufacturer, '%')))
          and (:status is null or c.status = :status)
    """)
    Page<SparePartStockDTO> searchSparePartStock(
            @Param("code") String code,
            @Param("name") String name,
            @Param("manufacturer") String manufacturer,
            @Param("status") PartStatus status,
            Pageable pageable
    );

    @Query("""
        select coalesce(sum(case when spi.transactionType = com.example.m6_thermal_power_plant_api.entity.enums.TransactionType.IMPORT then spi.quantity else -spi.quantity end), 0)
        from SparePartsInventory spi
        where spi.sparePart.id = :sparePartId and spi.isDeleted = false
    """)
    BigDecimal getStockQuantity(@Param("sparePartId") Integer sparePartId);
}
