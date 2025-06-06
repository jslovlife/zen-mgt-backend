package com.zenmgt.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@TableName("auth_user_credential")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuthUserCredential extends BaseDetailMapper {
    @TableField("parent_id")
    private Long parentId;

    @TableField("hash_password")
    private String hashPassword;

    @TableField("mfa_secret")
    private String mfaSecret;

    @TableField("mfa_enforced")
    @Builder.Default
    private Boolean mfaEnforced = false;

    @TableField("mfa_enabled")
    @Builder.Default
    private Boolean mfaEnabled = false;

    @TableField("recovery_codes")
    private String recoveryCodes;

    @Override
    public Long getParentId() {
        return parentId;
    }

    @Override
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
} 