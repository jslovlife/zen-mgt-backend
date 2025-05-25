package com.zenmgt.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "sys_approval_request_param")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SysApprovalRequestParam extends BaseVersionDetail {
    @Column(name = "sys_approval_id")
    private Long parentId;

    @Column(name = "param_type", nullable = false)
    private String paramType;

    @Column(name = "param_value", nullable = false)
    private String paramValue;

    @Override
    public Long getParentId() {
        return parentId;
    }

    @Override
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
} 