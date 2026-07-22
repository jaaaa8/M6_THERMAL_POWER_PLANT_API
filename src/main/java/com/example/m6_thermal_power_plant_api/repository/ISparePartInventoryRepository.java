package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.SparePartsInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ISparePartInventoryRepository extends JpaRepository<SparePartsInventory, Integer> {

    /**
     * Dashboard: đếm số loại vật tư có tồn kho thấp (net stock < threshold).
     * Net stock = SUM(IMPORT qty) - SUM(EXPORT qty) per spare_part_id.
     */
    @Query(value = """
        SELECT COUNT(*) FROM (
            SELECT i.spare_part_id,
                   SUM(CASE WHEN i.transaction_type = 'IMPORT' THEN i.quantity ELSE 0 END)
                 - SUM(CASE WHEN i.transaction_type = 'EXPORT' THEN i.quantity ELSE 0 END) AS net
            FROM spare_parts_inventory i
            WHERE i.is_deleted = false
            GROUP BY i.spare_part_id
            HAVING net < :threshold
        ) AS low
        """, nativeQuery = true)
    long countLowStock(@Param("threshold") int threshold);
}
