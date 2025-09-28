package com.likelion.hufjok.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "material")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Material {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    // 자료 설명
    @Column(columnDefinition = "TEXT")
    private String description;

    private String professor;

    private String courseName;

    private int year;

    private int semester;

    private String filePath;

    // 업로드 날짜
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 수정 날짜
    @Column(nullable = true)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Review> reviews;
}
