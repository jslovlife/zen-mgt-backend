package com.zenmgt.repository;

import com.zenmgt.entity.VersionControlled;
import com.zenmgt.entity.VersionDetail;
import com.zenmgt.enums.ApprovalStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

public abstract class AbstractVersionControlRepository<T extends VersionControlled, D extends VersionDetail, DTO> 
        implements VersionControlRepository<T, D, DTO> {

    @PersistenceContext
    protected EntityManager entityManager;

    protected final Class<T> entityClass;
    protected final Class<D> detailClass;

    protected AbstractVersionControlRepository(Class<T> entityClass, Class<D> detailClass) {
        this.entityClass = entityClass;
        this.detailClass = detailClass;
    }

    @Override
    @Transactional
    public VersionControlResult<T> createWithDetailAndApproval(DTO dto, Long entityId, Long detailId, Long approvalId, Long createdBy) {
        T entity = createMainEntity(dto, entityId);
        entityManager.persist(entity);

        D detail = createDetailEntity(dto, entity, detailId);
        entityManager.persist(detail);

        entity.setActiveVersion(detailId);
        entityManager.merge(entity);

        return VersionControlResult.<T>builder()
            .entity(entity)
            .approvalId(approvalId)
            .approvalStatus(ApprovalStatus.PENDING.getValue())
            .build();
    }

    @Override
    @Transactional
    public VersionControlResult<T> updateWithNewDetailAndApproval(Long entityId, DTO dto, Long newDetailId, Long approvalId, Long createdBy) {
        T entity = entityManager.find(entityClass, entityId);
        if (entity == null) {
            throw new RuntimeException("Entity not found: " + entityClass.getSimpleName() + " with ID " + entityId);
        }

        D detail = createDetailEntity(dto, entity, newDetailId);
        entityManager.persist(detail);

        entity.setActiveVersion(newDetailId);
        entityManager.merge(entity);

        return VersionControlResult.<T>builder()
            .entity(entity)
            .approvalId(approvalId)
            .approvalStatus(ApprovalStatus.PENDING.getValue())
            .build();
    }

    @Override
    public D getCurrentDetail(Long entityId) {
        T entity = entityManager.find(entityClass, entityId);
        if (entity == null || entity.getActiveVersion() == null) {
            return null;
        }
        return getDetailVersion(entityId, entity.getActiveVersion());
    }

    @Override
    public D getDetailVersion(Long entityId, Long detailId) {
        return entityManager.createQuery(
            "SELECT d FROM " + detailClass.getSimpleName() + " d WHERE d.parentId = :entityId AND d.id = :detailId",
            detailClass)
            .setParameter("entityId", entityId)
            .setParameter("detailId", detailId)
            .getResultStream()
            .findFirst()
            .orElse(null);
    }

    /**
     * Create a new main entity from DTO
     */
    protected abstract T createMainEntity(DTO dto, Long entityId);

    /**
     * Create a new detail entity from DTO
     */
    protected abstract D createDetailEntity(DTO dto, T entity, Long detailId);
} 