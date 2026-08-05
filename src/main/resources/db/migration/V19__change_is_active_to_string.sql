-- Step 1: Add a temp column
ALTER TABLE employees ADD COLUMN is_active_temp VARCHAR(50) DEFAULT 'ACTIVE';

-- Step 2: Update temp column based on bit value
UPDATE employees SET is_active_temp = 'ACTIVE' WHERE is_active = 1 OR is_active IS NULL;
UPDATE employees SET is_active_temp = 'INACTIVE' WHERE is_active = 0;

-- Step 3: Drop the old column
ALTER TABLE employees DROP COLUMN is_active;

-- Step 4: Rename the temp column to is_active
ALTER TABLE employees CHANGE COLUMN is_active_temp is_active VARCHAR(50) DEFAULT 'ACTIVE';
