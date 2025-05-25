package com.zenmgt.repository;

import com.zenmgt.entity.AuthUserDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthUserDetailRepository extends JpaRepository<AuthUserDetail, Long> {
} 