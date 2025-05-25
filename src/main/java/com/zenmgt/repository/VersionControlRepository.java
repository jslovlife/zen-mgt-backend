package com.zenmgt.repository;

import com.zenmgt.entity.VersionControlled;
import com.zenmgt.entity.VersionDetail;
import lombok.Builder;
import lombok.Data;

public interface VersionControlRepository<T extends VersionControlled, D extends VersionDetail, DTO> {
    
    @Data
    @Builder
    class VersionControlResult<T> {
        private T entity;
        private Long approvalId;
        private String approvalStatus;
    }

    /**
     * Creates a new entity with its first version detail and approval request
     * @param dto The DTO containing the data
     * @param entityId ID for the main entity
     * @param detailId ID for the detail entity
     * @param approvalId ID for the approval request
     * @param createdBy ID of the user creating the request
     * @return The created entity with approval information
     */
    VersionControlResult<T> createWithDetailAndApproval(DTO dto, Long entityId, Long detailId, Long approvalId, Long createdBy);

    /**
     * Updates an entity by creating a new version detail and approval request
     * @param entityId ID of the main entity to update
     * @param dto The DTO containing the updated data
     * @param newDetailId ID for the new detail version
     * @param approvalId ID for the approval request
     * @param createdBy ID of the user creating the request
     * @return The updated entity with approval information
     */
    VersionControlResult<T> updateWithNewDetailAndApproval(Long entityId, DTO dto, Long newDetailId, Long approvalId, Long createdBy);

    /**
     * Gets the current version detail for an entity
     * @param entityId ID of the main entity
     * @return The current version detail
     */
    D getCurrentDetail(Long entityId);

    /**
     * Gets a specific version detail for an entity
     * @param entityId ID of the main entity
     * @param detailId ID of the specific detail version
     * @return The requested version detail
     */
    D getDetailVersion(Long entityId, Long detailId);
} 