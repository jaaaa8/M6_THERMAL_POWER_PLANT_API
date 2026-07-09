CREATE TABLE repair_history (
                                id INT AUTO_INCREMENT PRIMARY KEY,

                                work_order_id INT,
                                equipment_id INT,

                                repair_date DATE,

                                repair_content TEXT,
                                repair_result TEXT,

                                is_deleted BIT(1) NOT NULL DEFAULT b'0',
                                deleted_at DATETIME NULL,

                                CONSTRAINT fk_repair_history_work_order
                                    FOREIGN KEY (work_order_id)
                                        REFERENCES work_orders(id),

                                CONSTRAINT fk_repair_history_equipment
                                    FOREIGN KEY (equipment_id)
                                        REFERENCES equipment(id)
);

CREATE TABLE repair_history_details (
                                        id INT AUTO_INCREMENT PRIMARY KEY,

                                        repair_history_id INT NOT NULL,
                                        spare_part_id INT NOT NULL,

                                        quantity INT NOT NULL DEFAULT 1,

                                        is_deleted BIT(1) NOT NULL DEFAULT b'0',
                                        deleted_at DATETIME NULL,

                                        CONSTRAINT fk_repair_history_detail_history
                                            FOREIGN KEY (repair_history_id)
                                                REFERENCES repair_history(id),

                                        CONSTRAINT fk_repair_history_detail_spare_part
                                            FOREIGN KEY (spare_part_id)
                                                REFERENCES spare_parts(id)
);

ALTER TABLE spare_parts_issue_details
    MODIFY COLUMN quantity INT;