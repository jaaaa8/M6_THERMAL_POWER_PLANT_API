package com.example.m6_thermal_power_plant_api.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Lớp cha dùng chung cho các entity có hỗ trợ xoá mềm (soft delete).
 *
 * CƠ CHẾ: mỗi entity con tự khai báo {@code @SQLRestriction("is_deleted = false")}
 * ngay trên class của nó (Hibernate KHÔNG cho kế thừa annotation này qua
 * {@code @MappedSuperclass}, nên class cha chỉ cung cấp field + helper method,
 * còn annotation @SQLRestriction phải khai báo lại ở từng entity con).
 *
 * TẠI SAO DÙNG @SQLRestriction THAY VÌ @SoftDelete (org.hibernate.annotations.SoftDelete)?
 * - @SoftDelete (Hibernate 6.4+) CẤM mọi @ManyToOne/@OneToOne fetch = LAZY trỏ TỚI
 *   entity được đánh dấu @SoftDelete → ném UnsupportedMappingException ngay lúc
 *   build SessionFactory (app không start được). Vì hầu hết entity nghiệp vụ ở
 *   đây đều có quan hệ LAZY trỏ tới Account/Equipment/Tool..., dùng @SoftDelete
 *   buộc phải đổi rất nhiều quan hệ sang EAGER (mất lợi ích lazy-loading).
 * - @SQLRestriction chỉ thêm điều kiện WHERE vào câu SQL khi Hibernate generate
 *   (cả query trực tiếp và query JOIN/khởi tạo proxy cho quan hệ LAZY) — KHÔNG
 *   có hạn chế nào với LAZY, nên giữ được toàn bộ quan hệ LAZY như mong muốn.
 *
 * LƯU Ý QUAN TRỌNG — khác với @SoftDelete:
 * @SQLRestriction KHÔNG tự biến repository.delete(entity) thành UPDATE.
 * Ở tầng service phải tự gọi {@code entity.softDelete()} rồi {@code repository.save(entity)},
 * KHÔNG gọi {@code repository.delete(entity)} cho các entity dùng cơ chế này.
 *
 * Rủi ro cần biết: nếu một entity KHÁC (không soft-delete) vẫn còn FK trỏ tới
 * một bản ghi đã bị soft-delete, và code cố tình truy cập quan hệ đó
 * (VD: child.getParent()), Hibernate sẽ không tìm thấy dòng (do bị lọc bởi
 * is_deleted = false) và ném ObjectNotFoundException. Vì vậy chỉ soft-delete
 * khi đã đảm bảo không còn tham chiếu "sống" nào cần đọc lại record đó.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseSoftDeleteEntity {

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = Boolean.FALSE;

    /** Đánh dấu xoá mềm. Nhớ gọi repository.save(entity) sau khi gọi hàm này. */
    public void softDelete() {
        this.isDeleted = Boolean.TRUE;
    }

    /** Khôi phục bản ghi đã xoá mềm. Nhớ gọi repository.save(entity) sau khi gọi hàm này. */
    public void restore() {
        this.isDeleted = Boolean.FALSE;
    }
}
