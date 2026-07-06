package com.example.m6_thermal_power_plant_api.repository.equipment;

import com.example.m6_thermal_power_plant_api.entity.EquipmentSystem;
import com.example.m6_thermal_power_plant_api.entity.enums.EquipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IEquipmentSystemRepository extends JpaRepository<EquipmentSystem, Integer> {
    @Query("""
SELECT s
FROM EquipmentSystem s
WHERE
(:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')))
AND
(:status IS NULL OR s.status = :status)
""")
    Page<EquipmentSystem> search(
            @Param("name") String name,
            @Param("status") EquipmentStatus status,
            Pageable pageable
    );
    boolean existsByNameIgnoreCase(String name);

    List<EquipmentSystem> findByCodeStartingWith (String prefix);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);

}
