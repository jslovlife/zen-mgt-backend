package com.zenmgt.entity;

public interface VersionControlled {
    Long getId();
    void setId(Long id);
    Long getActiveVersion();
    void setActiveVersion(Long activeVersion);
} 