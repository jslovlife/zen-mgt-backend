package com.zenmgt.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
public abstract class BaseVersionDetail implements VersionDetail {
    @Id
    private Long id;
    
    public abstract Long getParentId();
    public abstract void setParentId(Long parentId);
} 