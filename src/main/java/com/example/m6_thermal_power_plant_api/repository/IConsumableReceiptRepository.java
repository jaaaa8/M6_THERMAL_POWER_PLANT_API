package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.ConsumableReceipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface IConsumableReceiptRepository extends JpaRepository<ConsumableReceipt, Integer> {
    @Query(value = "select r from ConsumableReceipt r join fetch r.consumable join fetch r.receivedBy",
            countQuery = "select count(r) from ConsumableReceipt r")
    Page<ConsumableReceipt> findAllWithDetails(Pageable pageable);
}
