package com.example.m6_thermal_power_plant_api.dto.file;

/**
 * Kết quả một lần upload lên Cloudinary. Caller PHẢI LƯU {@code publicId} và
 * {@code resourceType} vào DB cùng với {@code secureUrl} nếu sau này cần xoá file.
 *
 * TẠI SAO: bài học từ project cũ (office_rental_md5) — chỉ lưu URL rồi parse
 * ngược ra public_id để xoá là cách làm HỎNG:
 *  - public_id có thể chứa folder ("work-orders/WO-123") nhưng parse từ URL dễ
 *    cắt mất prefix folder → destroy sai target.
 *  - resource_type lúc upload ("image") và lúc xoá ("raw") lệch nhau → Cloudinary
 *    không tìm thấy asset, xoá fail ÂM THẦM.
 * Lưu đủ (publicId, resourceType) ngay lúc upload thì xoá đúng by-construction.
 *
 * @param publicId     định danh asset trên Cloudinary (gồm cả folder) — dùng để xoá
 * @param secureUrl    URL https để hiển thị/tải file
 * @param resourceType "image" | "raw" | "video" — PHẢI truyền lại đúng giá trị này khi xoá
 * @param format       đuôi file trên Cloudinary (jpg, png, pdf...)
 * @param bytes        kích thước file đã upload
 */
public record FileUploadResult(
        String publicId,
        String secureUrl,
        String resourceType,
        String format,
        long bytes
) {
}
