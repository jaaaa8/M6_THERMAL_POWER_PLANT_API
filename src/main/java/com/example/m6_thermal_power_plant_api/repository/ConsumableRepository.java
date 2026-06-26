package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.Consumable;
import com.example.m6_thermal_power_plant_api.entity.enums.PartStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ConsumableRepository extends JpaRepository<Consumable, Integer> {
    boolean existsByConsumableCode(String consumableCode);
    boolean existsByConsumableCodeAndIdNot(String consumableCode, Integer id);
    Optional<Consumable> findByConsumableCodeIgnoreCase(String consumableCode);

    @Query("""
    select c
    from Consumable c
    where (:code is null or :code = '' or lower(c.consumableCode) like lower(concat('%', :code, '%')))
      and (:name is null or :name = '' or lower(c.name) like lower(concat('%', :name, '%')))
      and (:manufacturer is null or :manufacturer = '' or lower(c.manufacturer) like lower(concat('%', :manufacturer, '%')))
      and (:price is null or c.price = :price)
      and (:status is null or c.status = :status)
""")
    Page<Consumable> searchByFields(
            @Param("code") String code,
            @Param("name") String name,
            @Param("manufacturer") String manufacturer,
            @Param("price") BigDecimal price,
            @Param("status") PartStatus status,
            Pageable pageable
    );
}
