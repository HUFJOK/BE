package com.likelion.hufjok.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users") // 'user'는 DB 예약어일 수 있으므로 'users'를 추천합니다.
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false) // --- nickname 필드 추가 ---
    private String nickname;

    private String socialProvider;

    private String providerId;

    @Column(name = "is_onboarding_completed", nullable = false)
    @Builder.Default
    private Boolean isOnboardingCompleted = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private String major;

    private String minor;

    @Column(nullable = false) // --- points 필드 타입 수정 ---
    @Builder.Default
    private int points = 0;

    @Builder.Default
    private boolean bonusAwarded = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default // --- 초기화 및 이름 변경 ---
    private List<Material> materials = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default // --- 초기화 및 이름 변경 ---
    private List<Review> reviews = new ArrayList<>();

    // --- 타임스탬프 자동 생성 메소드 추가 ---
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


}