package com.example.rbac.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "sys_permission")
@Data
@EqualsAndHashCode(callSuper = true)
public class Permission extends BaseEntity {
    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Column(length = 200)
    private String description;

    @Column(length = 100)
    private String path;

    @Column(length = 50)
    private String method;
}