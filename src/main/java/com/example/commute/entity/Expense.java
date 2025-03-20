package com.example.commute.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "expense")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @ElementCollection // 여러 URL을 저장할 때 @ElementCollection을 사용
    @CollectionTable(name = "expense_photos", joinColumns = @JoinColumn(name = "expense_id"))
    @Column(name = "photo_url", length = 255)
    private List<String> photoUrls;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id") // user_id를 직접 매핑하도록 수정
    private User user;

    @Column(name = "created_at", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public enum Category {
        식비,
        교통,
        숙박,
        경조사,
        기타
    }
}
