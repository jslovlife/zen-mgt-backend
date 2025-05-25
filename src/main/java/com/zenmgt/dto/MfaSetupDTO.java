package com.zenmgt.dto;

import lombok.Data;

@Data
public class MfaSetupDTO {
    private String username;
    private String password;
    private String mfaCode;
    private String recoveryCode;
    private Boolean enableMfa;
} 