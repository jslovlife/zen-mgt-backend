package com.zenmgt.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@TableName("auth_user")
@EqualsAndHashCode(callSuper = true)
public class AuthUser extends BaseMasterMapper {
    @TableField("user_code")
    private String userCode;

    @TableField("session_key")
    private String sessionKey;

    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;
} 