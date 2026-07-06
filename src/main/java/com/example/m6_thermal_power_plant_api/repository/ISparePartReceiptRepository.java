package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.SparePartReceipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ISparePartReceiptRepository extends JpaRepository<SparePartReceipt, Integer> {
    @Query(value = "select r from SparePartReceipt r join fetch r.sparePart join fetch r.receivedBy",
            countQuery = "select count(r) from SparePartReceipt r")
    Page<SparePartReceipt> findAllWithDetails(Pageable pageable);
}
