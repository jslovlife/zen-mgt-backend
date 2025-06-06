package com.zenmgt.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@TableName("auth_user_detail")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuthUserDetail extends BaseDetailMapper {
    @TableField("parent_id")
    private Long parentId;

    @TableField("username")
    private String username;

    @TableField("email")
    private String email;

    @TableField("session_validity")
    @Builder.Default
    private Long sessionValidity = 86400000L; // Default to 24 hours (24 * 60 * 60 * 1000 ms)

    @Override
    public Long getParentId() {
        return parentId;
    }

    @Override
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
} 