-- V14 — Xoá sạch cơ chế phân quyền theo PERMISSION (V4/V5 seed), chuyển hẳn
-- sang phân quyền theo ROLE (xem SecurityConfig#roleHierarchy + @PreAuthorize
-- ở từng controller). Mô hình permission bị đánh giá là quá phức tạp và
-- không cần thiết cho quy mô nghiệp vụ hiện tại — quyết định của chủ dự án.
--
-- Thứ tự xoá: bảng con (role_permissions, FK tới cả roles lẫn permissions)
-- trước, rồi mới xoá bảng permissions; cột permission_version trên accounts
-- không còn được JwtAuthenticationFilter/AuthService tham chiếu (đã gỡ ở
-- code Java cùng đợt) nên xoá luôn.
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS permissions;
ALTER TABLE accounts DROP COLUMN permission_version;
