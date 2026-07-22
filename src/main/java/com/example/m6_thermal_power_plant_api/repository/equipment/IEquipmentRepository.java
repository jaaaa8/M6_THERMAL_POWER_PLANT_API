package com.example.m6_thermal_power_plant_api.repository.equipment;

import com.example.m6_thermal_power_plant_api.entity.Equipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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
            (:systemId IS NULL OR e.system_id = :systemId)
            AND (:kks IS NULL OR e.kks_code LIKE CONCAT('%',:kks,'%'))
            AND (:name IS NULL OR e.name LIKE CONCAT('%',:name,'%'))
            AND (:typeId IS NULL OR e.equipment_type_id=:typeId)
            AND (:status IS NULL OR e.status=:status)
            """,
            countQuery = """
        SELECT COUNT(*)
        FROM equipment e
        WHERE
        (:systemId IS NULL OR e.system_id = :systemId)
        AND (:kks IS NULL OR e.kks_code LIKE CONCAT('%',:kks,'%'))
        AND (:name IS NULL OR e.name LIKE CONCAT('%',:name,'%'))
        AND (:typeId IS NULL OR e.equipment_type_id=:typeId)
        AND (:status IS NULL OR e.status=:status)
        """,
            nativeQuery = true)
    Page<Equipment> getEquipment(
            @Param("systemId") Integer systemId,
            @Param("kks") String kks,
            @Param("name") String name,
            @Param("typeId") Integer typeId,
            @Param("status") String status,
            Pageable pageable
    );
    Page<Equipment> findBySystemId(Integer systemId,Pageable pageable);

    /** Dashboard: đếm thiết bị theo trạng thái → Pie chart */
    @Query("SELECT e.status, COUNT(e) FROM Equipment e GROUP BY e.status")
    List<Object[]> countByStatusGrouped();

    /** Dashboard: đếm tổng thiết bị (không bao gồm đã xóa mềm) */
    long count();

    @Query("""
    SELECT e
    FROM Equipment e
    WHERE e.system.id = :systemId
      AND e.kksCode LIKE CONCAT(:prefix, '%')
    ORDER BY e.kksCode DESC
    """)
    List<Equipment> findLatestEquipmentByPrefix(
            @Param("systemId") Integer systemId,
            @Param("prefix") String prefix,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "system",
            "equipmentType",
            "parameters",
            "parameters.parameter",
            "parameters.parameter.units"
    })
    Optional<Equipment> findWithDetailById(Integer id);
}
