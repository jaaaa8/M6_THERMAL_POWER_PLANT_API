-- ============================================================
-- SCMS - Equipment, Repair & Maintenance Management System
-- Thermal Power Plant
-- ============================================================
-- drop database SCMS;
create database if not exists SCMS;
use SCMS;

SET FOREIGN_KEY_CHECKS = 0;
SET NAMES utf8mb4;

-- ============================================================
-- 1. DEPARTMENTS & PERSONNEL
-- ============================================================

CREATE TABLE departments (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    department_code varchar (50) unique not null,
    name        VARCHAR(255) NOT NULL,
    description TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Phòng ban / Phân xưởng';


CREATE TABLE employees (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    employee_code   VARCHAR(50)  UNIQUE NOT NULL  COMMENT 'Mã nhân viên',
    full_name       VARCHAR(255) NOT NULL,
    gmail 			varchar(255) not null,
    phone 			varchar (12),
    department_id   INT,
    position        VARCHAR(255)              COMMENT 'Chức vụ',
    expertise       VARCHAR(255)              COMMENT 'Chuyên môn',
    is_active       BOOLEAN      DEFAULT TRUE,
    img_path 		text                  COMMENT 'Đường dẫn file đính kèm (IMG)',
    FOREIGN KEY (department_id) REFERENCES departments(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Nhân sự';


CREATE TABLE accounts (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    employee_id   INT  UNIQUE,
    username      VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255)        NOT NULL,
    is_active     BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Tài khoản đăng nhập';


CREATE TABLE roles (
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT 'Tên vai trò'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vai trò / Quyền';


CREATE TABLE account_roles (
    account_id  INT NOT NULL,
    role_id     INT NOT NULL,
    PRIMARY KEY (account_id, role_id),
    FOREIGN KEY (account_id) REFERENCES accounts(id),
    FOREIGN KEY (role_id)    REFERENCES roles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Phân quyền tài khoản';


-- ============================================================
-- 2. EQUIPMENT MANAGEMENT
-- ============================================================

CREATE TABLE systems (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255) NOT NULL COMMENT 'Tên hệ thống',
    description TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Hệ thống thiết bị (VD: Hệ thống xử lý nước thô)';


CREATE TABLE equipment_types (
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL COMMENT 'Loại thiết bị (Bơm, Quạt, Van, Động cơ...)',
    description text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Loại thiết bị';


CREATE TABLE equipment (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    kks_code            VARCHAR(50)  UNIQUE NOT NULL  COMMENT 'Mã KKS',
    name                VARCHAR(255) NOT NULL,
    system_id           INT,
    equipment_type_id   INT,
    status              VARCHAR(100)                  COMMENT 'Đang vận hành / Đang sửa chữa / Đang hỏng / Dự phòng',
    description         TEXT,
    img_path 		text                  COMMENT 'Đường dẫn file đính kèm (IMG)',
    FOREIGN KEY (system_id)         REFERENCES systems(id),
    FOREIGN KEY (equipment_type_id) REFERENCES equipment_types(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Thiết bị';


CREATE TABLE parameter_catalog (
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL COMMENT 'Tên thông số (VD: Công suất, Vòng quay...)',
    description text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Danh mục thông số thiết bị';


CREATE TABLE units (
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT 'Đơn vị đo (KW, bar, m3/h...)',
    description text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Đơn vị đo';


-- Mapping: each parameter can have multiple units
CREATE TABLE parameter_unit_map (
    parameter_id    INT NOT NULL,
    unit_id         INT NOT NULL,
    PRIMARY KEY (parameter_id, unit_id),
    FOREIGN KEY (parameter_id) REFERENCES parameter_catalog(id),
    FOREIGN KEY (unit_id)      REFERENCES units(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Đơn vị của từng loại thông số';


CREATE TABLE equipment_parameters (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    equipment_id    INT,
    parameter_id    INT,
    value           VARCHAR(255)    COMMENT 'Giá trị thông số',
    description     TEXT,
    FOREIGN KEY (equipment_id)  REFERENCES equipment(id),
    FOREIGN KEY (parameter_id)  REFERENCES parameter_catalog(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Thông số chi tiết của từng thiết bị';


-- ============================================================
-- 3. MATERIAL CATALOGS
-- ============================================================

CREATE TABLE spare_parts (
    id   INT AUTO_INCREMENT PRIMARY KEY,
    spare_part_code varchar(30) unique not null,
    name VARCHAR(255) NOT NULL COMMENT 'Tên vật tư thay thế (VD: Vòng bi SKF...)',
    price decimal(10,2) ,
    manufacturer varchar(100),
    img_path 		text                  COMMENT 'Đường dẫn file đính kèm (IMG)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vật tư thay thế';


CREATE TABLE consumable (
    id   INT AUTO_INCREMENT PRIMARY KEY,
    consumable_code varchar(30) unique not null,
    name VARCHAR(255) NOT NULL COMMENT 'Tên vật tư tiêu hao (VD: RP7, Dẻ lau, Dầu bôi trơn...)',
    price decimal(10,2) ,
    manufacturer varchar(100),
    img_path 		text                  COMMENT 'Đường dẫn file đính kèm (IMG)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vật tư tiêu hao';


-- ============================================================
-- 4. WAREHOUSE MANAGEMENT
-- ============================================================

-- Each row = one import or export transaction for spare parts
CREATE TABLE spare_parts_inventory (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    spare_part_id          INT                                     COMMENT 'Mã vật tư thay thế',
    supplier            VARCHAR(255)                            COMMENT 'Nhà cung cấp',
    account_id         INT                                     COMMENT 'Nhân viên thực hiện',
    quantity            DECIMAL(10,2)   DEFAULT 0,
    transaction_type    ENUM('IMPORT','EXPORT') NOT NULL        COMMENT 'Nhập / Xuất kho',
    transaction_date    DATETIME        DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (spare_part_id)    REFERENCES spare_parts(id),
	FOREIGN KEY (account_id) REFERENCES accounts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Kho vật tư thay thế (nhật ký nhập/xuất)';


-- Each row = one import or export transaction for consumables
CREATE TABLE consumable_inventory (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    consumable_id          INT                                     COMMENT 'Mã vật tư tiêu hao',
    supplier            VARCHAR(255)                            COMMENT 'Nhà cung cấp',
    account_id         INT                                     COMMENT 'Nhân viên thực hiện',
    quantity            DECIMAL(10,2)   DEFAULT 0,
    transaction_type    ENUM('IMPORT','EXPORT') NOT NULL        COMMENT 'Nhập / Xuất kho',
    transaction_date    DATETIME        DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (consumable_id)    REFERENCES consumable(id),
    FOREIGN KEY (account_id) REFERENCES accounts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Kho vật tư tiêu hao (nhật ký nhập/xuất)';


-- ============================================================
-- 5. REPAIR REQUEST & WORK ORDER
-- ============================================================

CREATE TABLE repair_requests (
    id                      INT AUTO_INCREMENT PRIMARY KEY,
    request_code            VARCHAR(50)  UNIQUE NOT NULL    COMMENT 'Mã yêu cầu',
    equipment_id            INT,
    requester_id            INT                             COMMENT 'Trưởng ca/kíp tạo yêu cầu',
    incident_description    TEXT                            COMMENT 'Mô tả sự cố',
    priority                enum ('high','low')             COMMENT 'Mức độ ưu tiên',
    status                  VARCHAR(100),
    created_at              DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (equipment_id)  REFERENCES equipment(id),
    FOREIGN KEY (requester_id)  REFERENCES accounts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Phiếu yêu cầu sửa chữa';


CREATE TABLE work_orders (
    id                      INT AUTO_INCREMENT PRIMARY KEY,
    order_code              VARCHAR(50)  UNIQUE NOT NULL    COMMENT 'Mã phiếu công tác',
    repair_request_id       INT          UNIQUE             COMMENT '1 yêu cầu → 1 phiếu công tác',
    leader_id               INT                             COMMENT 'Người lãnh đạo công việc',
    direct_supervisor_id    INT                             COMMENT 'Chỉ huy trực tiếp',
    safety_supervisor_id    INT                             COMMENT 'Người giám sát an toàn',
    start_time              DATETIME,
    end_time                DATETIME,
    status                  VARCHAR(100),
    pdf_path                VARCHAR(500)                    COMMENT 'Đường dẫn file PDF phiếu công tác',
    FOREIGN KEY (repair_request_id)     REFERENCES repair_requests(id),
    FOREIGN KEY (leader_id)             REFERENCES accounts(id),
    FOREIGN KEY (direct_supervisor_id)  REFERENCES accounts(id),
    FOREIGN KEY (safety_supervisor_id)  REFERENCES accounts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Phiếu công tác';


CREATE TABLE work_order_members (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    work_order_id   INT,
    account_id     INT,
    role_in_task    VARCHAR(255)    COMMENT 'Vai trò trong công việc',
    joined_at       DATETIME,
    left_at         DATETIME,
    FOREIGN KEY (work_order_id) REFERENCES work_orders(id),
    FOREIGN KEY (account_id)   REFERENCES accounts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Thành viên tham gia phiếu công tác';


CREATE TABLE work_order_extensions (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    work_order_id   INT,
    reason          TEXT            COMMENT 'Lý do gia hạn',
    extended_until  DATETIME        COMMENT 'Gia hạn đến ngày',
    approved_by     INT             COMMENT 'Người phê duyệt',
    FOREIGN KEY (work_order_id) REFERENCES work_orders(id),
    FOREIGN KEY (approved_by)   REFERENCES accounts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Gia hạn phiếu công tác';


-- ============================================================
-- 6. MATERIAL ISSUE SLIPS (linked to work orders)
-- ============================================================

-- Issued spare parts per work order
CREATE TABLE spare_parts_issues (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    spare_part_code  varchar(50) unique not null,
    work_order_id       INT,
    spare_part_id          INT                             COMMENT 'Vật tư thay thế được cấp',
    transaction_type    enum ('export','import')           COMMENT 'Xuất / Nhập',
    quantity            DECIMAL(10,2),
    issued_by           INT                             COMMENT 'Người thực hiện',
    issued_at           DATETIME,
    FOREIGN KEY (work_order_id) REFERENCES work_orders(id),
    FOREIGN KEY (spare_part_id)    REFERENCES spare_parts(id),
    FOREIGN KEY (issued_by)     REFERENCES accounts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Phiếu cấp vật tư thay thế';


-- Issued consumables per work order
CREATE TABLE consumable_issues (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    consumable_code  varchar(50) unique not null,
    work_order_id       INT,
    consumable_id          INT                             COMMENT 'Vật tư tiêu hao được cấp',
    transaction_type    enum ('export','import')                     COMMENT 'Xuất / Nhập',
    quantity            DECIMAL(10,2),
    issued_by           INT                             COMMENT 'Người thực hiện',
    issued_at           DATETIME,
    FOREIGN KEY (work_order_id) REFERENCES work_orders(id),
    FOREIGN KEY (consumable_id)    REFERENCES consumable(id),
    FOREIGN KEY (issued_by)     REFERENCES accounts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Phiếu cấp vật tư tiêu hao';


-- ============================================================
-- 7. TECHNICAL ASSESSMENT
-- ============================================================

CREATE TABLE technical_assessments (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    technical_code varchar(50) unique not null,
    work_order_id   INT,
    assessor_id     INT                             COMMENT 'Người đánh giá (Tổ trưởng)',
    result          TEXT                            COMMENT 'Kết quả đánh giá kỹ thuật',
    attachment_path VARCHAR(500)                    COMMENT 'Đường dẫn file đính kèm (PDF)',
    img_path 		text                  COMMENT 'Đường dẫn file đính kèm (IMG)',
    description text,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (work_order_id) REFERENCES work_orders(id),
    FOREIGN KEY (assessor_id)   REFERENCES accounts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Biên bản đánh giá kỹ thuật';


-- ============================================================
-- 8. TOOLS MANAGEMENT (CCDC)
-- ============================================================

CREATE TABLE tools (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    tool_code   VARCHAR(50)  UNIQUE NOT NULL    COMMENT 'Mã CCDC',
    name        VARCHAR(255) NOT NULL,
    quantity    INT DEFAULT 0                   COMMENT 'Số lượng hiện có trong kho',
    description text,
    img_path 		text                  COMMENT 'Đường dẫn file đính kèm (IMG)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Công cụ dụng cụ';


CREATE TABLE tool_borrow_logs (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    tool_id             INT,
    account_id         INT,
    quantity            INT,
    transaction_type    VARCHAR(50)     COMMENT 'BORROW (mượn) / RETURN (trả)',
    transaction_date    DATETIME,
    FOREIGN KEY (tool_id)       REFERENCES tools(id),
    FOREIGN KEY (account_id)   REFERENCES accounts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Nhật ký mượn / trả công cụ dụng cụ';


-- ============================================================
-- 9. LUBRICATION MAINTENANCE (Dầu mỡ)
-- ============================================================

CREATE TABLE lubrication_plans (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    equipment_id    INT,
    cycle_months    INT                     COMMENT 'Chu kỳ bảo dưỡng (tháng)',
    next_due_date   DATE                    COMMENT 'Ngày đến hạn tiếp theo',
    lubricant_type  VARCHAR(255)            COMMENT 'Loại dầu/mỡ',
    consumable_id      INT                     COMMENT 'Vật tư tiêu hao (dầu/mỡ)',
    quantity        DECIMAL(10,2),
    FOREIGN KEY (equipment_id)  REFERENCES equipment(id),
    FOREIGN KEY (consumable_id)    REFERENCES consumable(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Kế hoạch bảo dưỡng dầu mỡ';


CREATE TABLE lubrication_history (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    plan_id         INT,
    performed_date  DATE,
    notes           TEXT,
    FOREIGN KEY (plan_id) REFERENCES lubrication_plans(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Lịch sử bảo dưỡng dầu mỡ';


SET FOREIGN_KEY_CHECKS = 1;
