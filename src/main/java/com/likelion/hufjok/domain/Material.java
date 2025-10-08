package com.likelion.hufjok.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "material")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Material {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // --- 이 부분이 수정되었습니다 ---
    @Column(name = "professor_name") // DB 컬럼명은 snake_case로 명시
    private String professorName;     // 자바 필드명은 camelCase로 변경

    @Column(nullable = false)
    private String courseName;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private int semester;

    private String filePath;

    @Column(nullable = false, updatable = false) // updatable = false 추가
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default // --- 이 부분이 추가되었습니다 ---
    private List<Review> reviews = new ArrayList<>();

    // --- 아래 메소드들이 추가되었습니다 ---
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