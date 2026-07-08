-- ================================================================
--  V6 — Sửa account_roles (seed V3 gán role SAI)
--
--  V3 seed account_roles bị lệch: tài khoản `admin` bị gán role
--  MATERIALS_STOREKEEPER (id 2) thay vì ADMIN (id 10), nhiều tài khoản
--  bị gán sai loại, và account 7–30 không có role nào. Migration này xoá
--  toàn bộ mapping cũ và gán lại ĐÚNG theo tiền tố username → role.
--
--  Bản đồ role (theo V3 roles): 1=WORKER 2=MATERIALS_STOREKEEPER
--  3=TOOLS_STOREKEEPER 4=WORKSHOP_FOREMAN 5=SHIFT_LEADER 6=CREW_LEADER
--  7=MAINTENANCE_FOREMAN 8=TEAM_LEADER 9=SAFETY_SUPERVISOR 10=ADMIN
-- ================================================================

DELETE FROM account_roles;

INSERT INTO account_roles (account_id, role_id) VALUES
(1, 10),                       -- admin           -> ADMIN
(2, 9), (3, 9),                -- ssupervisor_*   -> SAFETY_SUPERVISOR
(4, 4), (5, 4),                -- wforeman_*      -> WORKSHOP_FOREMAN
(6, 7),                        -- mforeman_1      -> MAINTENANCE_FOREMAN
(7, 2), (8, 2),                -- mstorekeeper_*  -> MATERIALS_STOREKEEPER
(9, 3), (10, 3),               -- tstorekeeper_*  -> TOOLS_STOREKEEPER
(11, 5), (12, 5),              -- sleader_*       -> SHIFT_LEADER
(13, 6), (14, 6), (15, 6),     -- cleader_*       -> CREW_LEADER
(16, 8), (17, 8), (18, 8),     -- tleader_*       -> TEAM_LEADER
(19, 1), (20, 1), (21, 1), (22, 1), (23, 1), (24, 1),
(25, 1), (26, 1), (27, 1), (28, 1), (29, 1), (30, 1);  -- worker_* -> WORKER
