package com.likelion.hufjok.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "point")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Point {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private int amount;

    // 변동 사유
    @Column(nullable = true)
    private String reason;

    // 변동 발생 시간
    @Column(nullable = true)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // user_id 외래 키를 가집니다.
    @JsonIgnore
    private User user;
}
