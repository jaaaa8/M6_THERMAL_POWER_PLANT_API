package com.example.m6_thermal_power_plant_api.repository;

import com.example.m6_thermal_power_plant_api.entity.tool.Tool;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IToolRepository extends JpaRepository<Tool, Integer> {

    boolean existsByToolCode(String toolCode);

    /**
     * Tìm kiếm CCDC theo tên (keyword) và/hoặc chủng loại (categoryId).
     * Truyền null cho tham số nào không cần lọc.
     */
    @Query("""
            SELECT t FROM Tool t
            WHERE (:keyword IS NULL OR :keyword = ''
                   OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:categoryId IS NULL OR t.toolCategory.id = :categoryId)
            """)
    Page<Tool> search(@Param("keyword") String keyword,
                      @Param("categoryId") Integer categoryId,
                      Pageable pageable);

    /**
     * Lấy theo id, bao gồm cả bản ghi đã soft-delete (bỏ qua @SQLRestriction).
     */
    @Query(value = "SELECT * FROM tools WHERE id = :toolId", nativeQuery = true)
    Optional<Tool> findByIdIncludingDeleted(@Param("toolId") Integer toolId);
}