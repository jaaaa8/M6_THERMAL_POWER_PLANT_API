package com.example.m6_thermal_power_plant_api.repository.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.response.ListEquipmentDTO;
import com.example.m6_thermal_power_plant_api.entity.Equipment;
import com.example.m6_thermal_power_plant_api.entity.EquipmentSystem;
import com.example.m6_thermal_power_plant_api.entity.enums.EquipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IEquipmentRepository extends JpaRepository<Equipment, Integer> {


    @Query(value = "SELECT * FROM equipment WHERE id = :equipmentId", nativeQuery = true)
    Optional<Equipment> findByIdIncludingDeleted(@Param("equipmentId") Integer equipmentId);

    /**
     * Lấy thiết bị mới nhất (id lớn nhất), kể cả bản ghi đã bị xoá mềm.
     */
    @Query(value = "SELECT * FROM equipment ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Optional<Equipment> findLatestIncludingDeleted();

    @Query(value = """

            SELECT e.*
            FROM equipment e
            WHERE
            (:kks IS NULL OR e.kks_code LIKE CONCAT('%',:kks,'%'))
            AND (:name IS NULL OR e.name LIKE CONCAT('%',:name,'%'))
            AND (:typeId IS NULL OR e.equipment_type_id=:typeId)
            AND (:status IS NULL OR e.status=:status)
            """,
            countQuery = """
        SELECT COUNT(*)
        FROM equipment e
        WHERE
        (:kks IS NULL OR e.kks_code LIKE CONCAT('%',:kks,'%'))
        AND (:name IS NULL OR e.name LIKE CONCAT('%',:name,'%'))
        AND (:typeId IS NULL OR e.equipment_type_id=:typeId)
        AND (:status IS NULL OR e.status=:status)
        """,
            nativeQuery = true)
    Page<Equipment> getEquipment(
            @Param("kks") String kks,
            @Param("name") String name,
            @Param("typeId") Integer typeId,
            @Param("status") String status,
            Pageable pageable
    );

    Page<Equipment> findBySystemId(Integer systemId,Pageable pageable);

}
