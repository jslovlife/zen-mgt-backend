package com.zenmgt.repository;

import com.zenmgt.entity.AuthUserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthUserCredentialRepository extends JpaRepository<AuthUserCredential, Long> {
    Optional<AuthUserCredential> findByParentId(Long parentId);
} 