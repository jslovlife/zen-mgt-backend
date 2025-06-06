package com.zenmgt.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
public abstract class BaseDetailMapper {
    
    @TableId(type = IdType.INPUT)
    private Long id;

    @TableField("parent_id")
    private Long parentId;
    
    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("created_by")
    private Long createdBy;

    @TableField("updated_by")
    private Long updatedBy;

} 