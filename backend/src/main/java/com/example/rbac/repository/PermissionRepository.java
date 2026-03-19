package com.example.rbac.repository;

import com.example.rbac.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    
    Optional<Permission> findByCode(String code);
    
    boolean existsByCode(String code);
    
    @Query("SELECT p FROM Permission p WHERE " +
           "(:keyword IS NULL OR p.name LIKE %:keyword% OR p.code LIKE %:keyword%)")
    Page<Permission> searchPermissions(@Param("keyword") String keyword, Pageable pageable);
    
    Set<Permission> findByIdIn(Set<Long> ids);
}
