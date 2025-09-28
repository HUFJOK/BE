package com.likelion.hufjok.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "user_material")
public class UserMaterial {

    // Many to Many 를 없애고자 만든 도메인

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


}