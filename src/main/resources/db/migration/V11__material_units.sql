-- V11: Đơn vị tính cho VẬT TƯ (lít, kg, cái, chai) + gán unit_id cho catalog mẫu.
-- V3 seed để unit_id = NULL trên cả 8 dòng consumable/spare_parts (units 1-7 là
-- đơn vị THÔNG SỐ thiết bị: kW, A, V...). Query tồn kho từng inner-join qua
-- c.unit nên trang Nhập/Xuất trống — query đã đổi sang LEFT JOIN, migration này
-- chỉ bổ sung dữ liệu cho đẹp/đúng nghiệp vụ.

INSERT INTO `units` (`id`, `name`, `description`, `is_deleted`) VALUES
  (8,  'lít',  'Lít (dung tích)', false),
  (9,  'kg',   'Kilogram', false),
  (10, 'cái',  'Cái / chiếc', false),
  (11, 'chai', 'Chai / bình xịt', false);

UPDATE `consumable` SET `unit_id` = 8  WHERE `consumable_code` = 'CON-LUBE-68'    AND `unit_id` IS NULL;
UPDATE `consumable` SET `unit_id` = 9  WHERE `consumable_code` = 'CON-GREASE-EP2' AND `unit_id` IS NULL;
UPDATE `consumable` SET `unit_id` = 10 WHERE `consumable_code` = 'CON-RAG'        AND `unit_id` IS NULL;
UPDATE `consumable` SET `unit_id` = 11 WHERE `consumable_code` = 'CON-RP7'        AND `unit_id` IS NULL;

UPDATE `spare_parts` SET `unit_id` = 10
WHERE `spare_part_code` IN ('SP-BRG-6312', 'SP-SEAL-050', 'SP-MTR-CONT', 'SP-VLV-GASK')
  AND `unit_id` IS NULL;
