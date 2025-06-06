package com.zenmgt.model;

import com.baomidou.mybatisplus.annotation.*;
import com.zenmgt.enums.RecordStatus;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
public abstract class BaseMasterMapper {
    
    @TableId(type = IdType.INPUT)
    private Long id;
    
    @TableField("active_version")
    private Long activeVersion;

    @TableField("record_status")
    private Integer recordStatus = 0; // Default to INACTIVE

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("created_by")
    private Long createdBy;

    @TableField("updated_by")
    private Long updatedBy;

    // Helper methods for RecordStatus
    public RecordStatus getRecordStatusEnum() {
        return recordStatus != null ? RecordStatus.fromCode(recordStatus) : RecordStatus.INACTIVE;
    }

    public void setRecordStatusEnum(RecordStatus status) {
        this.recordStatus = status.getCode();
    }

    public boolean isActive() {
        return recordStatus != null && recordStatus == 1;
    }

    public boolean isDeleted() {
        return recordStatus != null && recordStatus == 5;
    }

    public boolean isPendingApproval() {
        return recordStatus != null && 
               (recordStatus == 2 || recordStatus == 3 || recordStatus == 4);
    }
} 