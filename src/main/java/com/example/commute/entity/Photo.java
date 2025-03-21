package com.example.commute.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName; // 파일 이름
    private String filePath; // 파일 경로
    private String fileType; // 파일 타입 (이미지, PDF 등)
    private Long fileSize; // 파일 크기

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt; // 파일 업로드 시간

    private String description; // 파일 설명 (선택적)

    // Expense와의 관계 (다수의 사진이 하나의 비용에 속할 수 있음)
    @ManyToOne
    @JoinColumn(name = "expense_id")
    private Expense expense;

    public Photo(String fileName, String filePath, Expense expense) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.expense = expense;
    }
}
