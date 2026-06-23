package com.example.m6_thermal_power_plant_api.service.soft_delete;

import com.example.m6_thermal_power_plant_api.entity.base.BaseSoftDeleteEntity;
import com.example.m6_thermal_power_plant_api.entity.base.CascadeSoftDelete;
import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PersistenceUnitUtil;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class SoftDeleteCascadeService {
    private final EntityManager entityManager;

    public SoftDeleteCascadeService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public void softDelete(BaseSoftDeleteEntity root) {
        // Một mốc thời gian DUY NHẤT cho cả lần cascade → restore gom nhóm chính xác.
        cascadeSoftDelete(root, LocalDateTime.now(), new HashSet<>());
        entityManager.flush();
    }

    private void cascadeSoftDelete(BaseSoftDeleteEntity entity, LocalDateTime deletedAt, Set<EntityKey> visited) {
        BaseSoftDeleteEntity managedEntity = attach(entity);
        EntityKey entityKey = createEntityKey(managedEntity);
        if (!visited.add(entityKey)) {
            return;
        }

        List<BaseSoftDeleteEntity> dependents = findActiveDependents(managedEntity);

        if (!Boolean.TRUE.equals(managedEntity.getIsDeleted())) {
            managedEntity.softDelete(deletedAt);
        }

        for (BaseSoftDeleteEntity dependent : dependents) {
            cascadeSoftDelete(dependent, deletedAt, visited);
        }
    }

    /**
     * Khôi phục (restore) ngược lại toàn bộ cây đã bị {@link #softDelete} cascade:
     * bật lại is_deleted = false cho root và mọi dependent đã bị ẩn theo nó.
     *
     * VÌ SAO PHẢI DÙNG NATIVE SQL Ở ĐÂY?
     * Các entity đều gắn {@code @SQLRestriction("is_deleted = false")} nên mọi câu
     * JPQL/criteria sẽ KHÔNG nhìn thấy bản ghi đã xoá mềm → không thể tìm dependent
     * để khôi phục. Native SQL không bị @SQLRestriction lọc, nên ta dùng native query
     * để dò các dependent đang ở trạng thái is_deleted = true rồi bật lại.
     *
     * GOM NHÓM THEO deleted_at: chỉ khôi phục những dependent có cùng mốc deleted_at
     * với root — tức là những bản ghi đã bị xoá CÙNG LÔ với root. Tránh "vô tình hồi sinh"
     * các dependent vốn đã bị xoá riêng từ trước. Nếu root không có deleted_at (dữ liệu
     * cũ xoá trước khi có cột này) thì khôi phục mọi dependent đang bị ẩn (hành vi cũ).
     */
    @Transactional
    public void restore(BaseSoftDeleteEntity root) {
        Class<?> rootClass = Hibernate.getClass(root);
        PersistenceUnitUtil persistenceUnitUtil = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        Object rootId = persistenceUnitUtil.getIdentifier(root);
        if (rootId == null) {
            throw new IllegalArgumentException("Cannot cascade restore a transient entity: " + rootClass.getName());
        }
        cascadeRestore(rootClass, rootId, root.getDeletedAt(), new HashSet<>());
        // Bỏ các entity đang được quản lý trong context để lần đọc sau lấy lại dữ liệu mới.
        entityManager.flush();
        entityManager.clear();
    }

    private void cascadeRestore(Class<?> entityClass, Object entityId, LocalDateTime batchDeletedAt, Set<EntityKey> visited) {
        if (!visited.add(new EntityKey(entityClass, entityId))) {
            return;
        }

        String table = tableName(entityClass);
        String idColumn = idColumnName(entityClass);

        entityManager.createNativeQuery(
                        "update " + table + " set is_deleted = false, deleted_at = null where " + idColumn + " = :id and is_deleted = true")
                .setParameter("id", entityId)
                .executeUpdate();

        for (DeletedDependent dependent : findDeletedDependents(entityClass, entityId, batchDeletedAt)) {
            cascadeRestore(dependent.entityClass(), dependent.id(), batchDeletedAt, visited);
        }
    }

    private List<DeletedDependent> findDeletedDependents(Class<?> parentClass, Object parentId, LocalDateTime batchDeletedAt) {
        List<DeletedDependent> dependents = new ArrayList<>();

        for (EntityType<?> entityType : entityManager.getMetamodel().getEntities()) {
            Class<?> candidateClass = entityType.getJavaType();
            if (!BaseSoftDeleteEntity.class.isAssignableFrom(candidateClass)) {
                continue;
            }

            for (SingularAttribute<?, ?> attribute : entityType.getSingularAttributes()) {
                if (!isCascadeReferenceTo(attribute, parentClass)) {
                    continue;
                }

                String childTable = tableName(candidateClass);
                String childIdColumn = idColumnName(candidateClass);
                String foreignKeyColumn = joinColumnName(attribute);

                String sql = "select " + childIdColumn + " from " + childTable
                        + " where " + foreignKeyColumn + " = :parentId and is_deleted = true";
                if (batchDeletedAt != null) {
                    sql += " and deleted_at = :batchDeletedAt";
                }

                var query = entityManager.createNativeQuery(sql).setParameter("parentId", parentId);
                if (batchDeletedAt != null) {
                    query.setParameter("batchDeletedAt", batchDeletedAt);
                }
                List<?> childIds = query.getResultList();

                for (Object childId : childIds) {
                    dependents.add(new DeletedDependent(candidateClass, ((Number) childId).intValue()));
                }
            }
        }

        return dependents;
    }

    private String tableName(Class<?> entityClass) {
        Table table = entityClass.getAnnotation(Table.class);
        if (table != null && !table.name().isBlank()) {
            return table.name();
        }
        return entityClass.getSimpleName();
    }

    private String idColumnName(Class<?> entityClass) {
        for (Class<?> current = entityClass; current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    Column column = field.getAnnotation(Column.class);
                    if (column != null && !column.name().isBlank()) {
                        return column.name();
                    }
                    return field.getName();
                }
            }
        }
        throw new IllegalStateException("No @Id field found on " + entityClass.getName());
    }

    private String joinColumnName(SingularAttribute<?, ?> attribute) {
        Member member = attribute.getJavaMember();
        if (member instanceof Field field) {
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
            if (joinColumn != null && !joinColumn.name().isBlank()) {
                return joinColumn.name();
            }
        }
        throw new IllegalStateException("No @JoinColumn found on attribute " + attribute.getName());
    }

    private BaseSoftDeleteEntity attach(BaseSoftDeleteEntity entity) {
        if (entityManager.contains(entity)) {
            return entity;
        }
        return entityManager.merge(entity);
    }

    private EntityKey createEntityKey(BaseSoftDeleteEntity entity) {
        PersistenceUnitUtil persistenceUnitUtil = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        Object entityId = persistenceUnitUtil.getIdentifier(entity);
        if (entityId == null) {
            throw new IllegalArgumentException("Cannot cascade soft-delete a transient entity: " + entity.getClass().getName());
        }
        return new EntityKey(Hibernate.getClass(entity), entityId);
    }

    private List<BaseSoftDeleteEntity> findActiveDependents(BaseSoftDeleteEntity parent) {
        Class<?> parentClass = Hibernate.getClass(parent);
        List<BaseSoftDeleteEntity> dependents = new ArrayList<>();

        for (EntityType<?> entityType : entityManager.getMetamodel().getEntities()) {
            Class<?> candidateClass = entityType.getJavaType();
            if (!BaseSoftDeleteEntity.class.isAssignableFrom(candidateClass)) {
                continue;
            }

            for (SingularAttribute<?, ?> attribute : entityType.getSingularAttributes()) {
                if (!isCascadeReferenceTo(attribute, parentClass)) {
                    continue;
                }

                String query = "select e from " + entityType.getName() + " e where e." + attribute.getName() + " = :parent";
                entityManager.createQuery(query, candidateClass)
                        .setParameter("parent", parent)
                        .getResultList()
                        .stream()
                        .map(BaseSoftDeleteEntity.class::cast)
                        .forEach(dependents::add);
            }
        }

        return dependents;
    }

    private boolean isCascadeReferenceTo(SingularAttribute<?, ?> attribute, Class<?> parentClass) {
        Attribute.PersistentAttributeType attributeType = attribute.getPersistentAttributeType();
        if (attributeType != Attribute.PersistentAttributeType.MANY_TO_ONE
                && attributeType != Attribute.PersistentAttributeType.ONE_TO_ONE) {
            return false;
        }

        Class<?> referenceType = attribute.getJavaType();
        if (!referenceType.isAssignableFrom(parentClass)) {
            return false;
        }

        java.lang.reflect.Member member = attribute.getJavaMember();
        if (member instanceof java.lang.reflect.Field) {
            java.lang.reflect.Field field = (java.lang.reflect.Field) member;
            return field.isAnnotationPresent(CascadeSoftDelete.class);
        }

        return false;
    }

    private record EntityKey(Class<?> entityClass, Object entityId) {
        private EntityKey {
            Objects.requireNonNull(entityClass);
            Objects.requireNonNull(entityId);
        }
    }

    /** Một bản ghi dependent đã bị xoá mềm, cần khôi phục theo cha. */
    private record DeletedDependent(Class<?> entityClass, Object id) {
        private DeletedDependent {
            Objects.requireNonNull(entityClass);
            Objects.requireNonNull(id);
        }
    }
}
