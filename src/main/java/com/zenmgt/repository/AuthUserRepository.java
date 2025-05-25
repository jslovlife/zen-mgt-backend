package com.zenmgt.repository;

import com.zenmgt.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, Long>, AuthUserRepositoryCustom {
    @Query("SELECT u FROM AuthUser u JOIN FETCH AuthUserDetail d ON d.parentId = u.id WHERE LOWER(d.username) = LOWER(:username)")
    Optional<AuthUser> findByUsername(@Param("username") String username);
    
    @Query("SELECT u FROM AuthUser u JOIN FETCH AuthUserDetail d ON d.parentId = u.id WHERE LOWER(d.email) = LOWER(:email)")
    Optional<AuthUser> findByEmail(@Param("email") String email);
    
    Optional<AuthUser> findByUserCode(String userCode);
    boolean existsByUserCode(String userCode);
} 