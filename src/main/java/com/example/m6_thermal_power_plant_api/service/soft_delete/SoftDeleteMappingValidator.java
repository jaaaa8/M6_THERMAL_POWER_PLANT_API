package com.example.m6_thermal_power_plant_api.service.soft_delete;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.example.m6_thermal_power_plant_api.entity.base.CascadeSoftDelete;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import org.hibernate.annotations.SQLRestriction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;

/**
 * Kiểm tra cấu hình soft-delete lúc khởi động (fail-fast) để khỏi "dính bẫy" lúc chạy.
 *
 * Cơ chế soft-delete ở đây dựa trên 2 quy ước thủ công, rất dễ quên khi thêm entity mới:
 *  1) Mọi entity kế thừa {@link BaseSoftDeleteEntity} PHẢI tự khai báo
 *     {@code @SQLRestriction("is_deleted = false")} — quên thì bản ghi đã xoá mềm vẫn hiện ra.
 *  2) Mọi quan hệ @ManyToOne/@OneToOne trỏ TỚI một entity soft-delete nên cân nhắc gắn
 *     {@link CascadeSoftDelete}; nếu không, khi entity cha bị xoá mềm mà bản ghi con vẫn
 *     "sống" và đọc quan hệ này, Hibernate sẽ ném ObjectNotFoundException (do @SQLRestriction
 *     lọc mất dòng cha).
 *
 * Vi phạm (1) là lỗi chắc chắn → THROW chặn khởi động.
 * Trường hợp (2) có thể là cố ý (vd: không muốn xoá WorkOrder khi xoá Account) → chỉ WARN
 * để dev rà soát, không chặn khởi động.
 */
@Component
public class SoftDeleteMappingValidator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SoftDeleteMappingValidator.class);

    private final EntityManager entityManager;

    public SoftDeleteMappingValidator(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> missingRestriction = new ArrayList<>();
        List<String> nonCascadedReferences = new ArrayList<>();
        int softDeleteEntityCount = 0;

        for (EntityType<?> entityType : entityManager.getMetamodel().getEntities()) {
            Class<?> entityClass = entityType.getJavaType();

            if (BaseSoftDeleteEntity.class.isAssignableFrom(entityClass)) {
                softDeleteEntityCount++;
                if (!entityClass.isAnnotationPresent(SQLRestriction.class)) {
                    missingRestriction.add(entityClass.getSimpleName()
                            + " extends BaseSoftDeleteEntity but is missing @SQLRestriction(\"is_deleted = false\") "
                            + "— soft-deleted rows of this entity will stay visible.");
                }
            }

            for (SingularAttribute<?, ?> attribute : entityType.getSingularAttributes()) {
                Attribute.PersistentAttributeType type = attribute.getPersistentAttributeType();
                if (type != Attribute.PersistentAttributeType.MANY_TO_ONE
                        && type != Attribute.PersistentAttributeType.ONE_TO_ONE) {
                    continue;
                }
                Class<?> targetType = attribute.getJavaType();
                if (!BaseSoftDeleteEntity.class.isAssignableFrom(targetType)) {
                    continue;
                }
                if (!hasCascadeSoftDelete(attribute)) {
                    nonCascadedReferences.add(entityType.getJavaType().getSimpleName() + "." + attribute.getName()
                            + " -> " + targetType.getSimpleName() + " (not @CascadeSoftDelete)");
                }
            }
        }

        if (!nonCascadedReferences.isEmpty()) {
            log.warn("Soft-delete: {} reference(s) to soft-deletable entities are NOT @CascadeSoftDelete. "
                            + "Make sure each is intentional — reading one whose target has been soft-deleted throws ObjectNotFoundException:",
                    nonCascadedReferences.size());
            nonCascadedReferences.forEach(reference -> log.warn("    - {}", reference));
        }

        if (!missingRestriction.isEmpty()) {
            throw new IllegalStateException("Invalid soft-delete mapping:\n    - "
                    + String.join("\n    - ", missingRestriction));
        }

        log.info("Soft-delete mapping validated: {} soft-deletable entities, all declare @SQLRestriction.",
                softDeleteEntityCount);
    }

    private boolean hasCascadeSoftDelete(SingularAttribute<?, ?> attribute) {
        Member member = attribute.getJavaMember();
        return member instanceof Field field && field.isAnnotationPresent(CascadeSoftDelete.class);
    }
}
