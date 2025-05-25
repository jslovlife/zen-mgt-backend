package com.zenmgt.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
public abstract class BaseVersionControlled implements VersionControlled {
    @Id
    private Long id;
    
    @Column(name = "active_version")
    private Long activeVersion;
} 