package com.zenmgt.dto;

import lombok.Data;
 
@Data
public class MfaVerificationDTO {
    private String username;
    private String mfaCode;
} 