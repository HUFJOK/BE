package com.likelion.hufjok.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "user_material")
public class UserMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 복합키 대신 단순 기본키를 사용하는 것이 더 쉽습니다.

    // 🌟 1. Material (자료) 와의 다대일 관계 (자료 ID를 외래키로 가짐)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    // 🌟 2. User (사용자) 와의 다대일 관계 (사용자 ID를 외래키로 가짐)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


}