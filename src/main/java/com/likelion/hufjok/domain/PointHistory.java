package com.likelion.hufjok.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 전체 포인트 이력 누적용
public class PointHistory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 거래로 인해 더해지는 금액
    private int amountChange;

    // 변화 후 잔액
    @Column(nullable = false)
    private int amountAfter;

    private String reason;

    // 포인트가 자료 구매인지 업로드 보상인지 등
    @Enumerated(EnumType.STRING)
    @Column(name = "point_type", nullable = false, length = 20)
    private PointType type;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @PrePersist
    protected  void onCreate() {this.createdAt = LocalDateTime.now();}

    // 자료 업로드, 자료 구매, 관리자가 포인트 수정, 회원가입 시
    public enum PointType{
        EARN, USE, ADJUST, SIGNUP_BONUS;

        public int signedAmount(int amount) {
            return switch(this) {
                case EARN, SIGNUP_BONUS -> amount;
                case USE -> -amount;
                case ADJUST ->  amount;
            };
        }
    }
}
