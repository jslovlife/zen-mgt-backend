package com.zenmgt.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
public class AuthUserDTO {
    // Auth User fields
    private Long id;
    private String userCode;
    private Integer isActive;
    private String sessionKey;
    private Integer activeVersion;
    private LocalDateTime lastLoginAt;
    
    // Auth User Detail fields
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String profilePictureUrl;
    private String status;
    
    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
} 