package com.example.rbac.repository;

import com.example.rbac.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByCode(String code);
    
    boolean existsByCode(String code);
    
    @Query("SELECT r FROM Role r WHERE " +
           "(:keyword IS NULL OR r.name LIKE %:keyword% OR r.code LIKE %:keyword%)")
    Page<Role> searchRoles(@Param("keyword") String keyword, Pageable pageable);
}
