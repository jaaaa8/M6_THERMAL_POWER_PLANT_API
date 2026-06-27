-- ============================================================================
-- Soft-delete-aware UNIQUE constraints (fixes the "cannot re-create a code that
-- belongs to a soft-deleted row" problem).
--
-- PROBLEM
--   Columns like kks_code / employee_code / username are UNIQUE at the database
--   level, which knows nothing about is_deleted. After you soft-delete a row,
--   its code value is still occupied, so inserting a NEW row with that same code
--   fails with a duplicate-key error even though the old row is "hidden".
--
-- FIX (MySQL 8)
--   1. Add a STORED generated column `active_flag` = 1 when active, NULL when deleted.
--   2. Replace the single-column UNIQUE(code) with a composite UNIQUE(code, active_flag).
--      - Two ACTIVE rows  -> (code, 1) vs (code, 1) -> collision -> still rejected. OK.
--      - Active vs deleted -> (code, 1) vs (code, NULL) -> allowed (you can reuse the code).
--      - Two DELETED rows -> (code, NULL) vs (code, NULL) -> allowed (NULLs are distinct
--        in a MySQL unique index), so deletion history never collides.
--   `active_flag` is auto-maintained by MySQL from is_deleted; no application code needed.
--
-- HOW TO RUN
--   Run this ONCE against the m6_thermal_power_plant database (same way you run
--   sample-data.sql). It is idempotent — safe to re-run, and re-run it after any
--   schema rebuild (since ddl-auto recreates the original single-column indexes).
--
-- NOTE
--   The matching @Column(unique = true) flags were removed from the entities so
--   Hibernate ddl-auto no longer recreates the conflicting single-column indexes.
--   Uniqueness of these codes is now owned entirely by this script.
-- ============================================================================

USE m6_thermal_power_plant;

DELIMITER $$

DROP PROCEDURE IF EXISTS fix_soft_delete_unique $$

CREATE PROCEDURE fix_soft_delete_unique(IN p_table VARCHAR(64), IN p_code VARCHAR(64))
BEGIN
    DECLARE v_db          VARCHAR(64);
    DECLARE v_index_name  VARCHAR(64);
    DECLARE v_uk_name     VARCHAR(64);
    SET v_db      = DATABASE();
    SET v_uk_name = CONCAT('uk_', p_table, '_', p_code, '_active');

    -- 1) Add the generated active_flag column if it is not there yet.
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = v_db AND table_name = p_table AND column_name = 'active_flag'
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `active_flag` TINYINT ',
                          'GENERATED ALWAYS AS (IF(CAST(`is_deleted` AS UNSIGNED), NULL, 1)) STORED');
        PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    END IF;

    -- 2) Drop any leftover single-column UNIQUE index on p_code (the old one).
    drop_loop: LOOP
        SET v_index_name = (
            SELECT index_name
            FROM information_schema.statistics
            WHERE table_schema = v_db AND table_name = p_table
              AND non_unique = 0 AND index_name <> 'PRIMARY'
            GROUP BY index_name
            HAVING COUNT(*) = 1 AND MAX(column_name) = p_code
            LIMIT 1
        );
        IF v_index_name IS NULL THEN
            LEAVE drop_loop;
        END IF;
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` DROP INDEX `', v_index_name, '`');
        PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    END LOOP;

    -- 3) Create the composite UNIQUE(code, active_flag) if not present.
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = v_db AND table_name = p_table AND index_name = v_uk_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD CONSTRAINT `', v_uk_name, '` ',
                          'UNIQUE (`', p_code, '`, `active_flag`)');
        PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    END IF;
END $$

DELIMITER ;

-- Apply to every table that has a unique business code.
CALL fix_soft_delete_unique('equipment',             'kks_code');
CALL fix_soft_delete_unique('employees',             'employee_code');
CALL fix_soft_delete_unique('accounts',              'username');
CALL fix_soft_delete_unique('departments',           'department_code');
CALL fix_soft_delete_unique('tools',                 'tool_code');
CALL fix_soft_delete_unique('tool_categories',       'category_code');
CALL fix_soft_delete_unique('spare_parts',           'spare_part_code');
CALL fix_soft_delete_unique('consumable',            'consumable_code');
CALL fix_soft_delete_unique('repair_requests',       'request_code');
CALL fix_soft_delete_unique('work_orders',           'order_code');
CALL fix_soft_delete_unique('technical_assessments', 'technical_code');
CALL fix_soft_delete_unique('spare_parts_issues',    'spare_part_code');
CALL fix_soft_delete_unique('consumable_issues',     'consumable_code');

DROP PROCEDURE fix_soft_delete_unique;
