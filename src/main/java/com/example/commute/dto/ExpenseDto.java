package com.example.commute.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data  // getter, setter, toString 등 자동 생성
@Builder
public class ExpenseDto {
    private Long id;
    private String title;
    private String category;
    private String content;
    private String photoUrl;
    private Double amount;
    private LocalDateTime createdAt;

    private String name;

    @JsonCreator
    public ExpenseDto(
            @JsonProperty("id") Long id,
            @JsonProperty("title") String title,
            @JsonProperty("category") String category,
            @JsonProperty("content") String content,
            @JsonProperty("photoUrl") String photoUrl,
            @JsonProperty("amount") Double amount,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("name") String name
    ) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.content = content;
        this.photoUrl = photoUrl;
        this.amount = amount;
        this.createdAt = createdAt;
        this.name = name;
    }
}
